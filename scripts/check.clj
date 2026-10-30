;; CI check for the catalogue. Run with: bb check (Malli comes from bb.edn).
;; Roles and skills are versioned by folder: roles/<name>/<version>/..., skills/<name>/<version>/...
;; A released version is never edited, and the root of each role and skill links to its latest.
(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.java.shell :as sh]
         '[clojure.string :as str]
         '[malli.core :as m]
         '[malli.error :as me])

(def schema (m/schema (edn/read-string (slurp "schema/role.edn"))))

(def known-agents #{"claude-code" "codex" "pi"})

(defn subdirs [path]
  (->> (.listFiles (io/file path))
       (filter #(.isDirectory %))
       (sort-by #(.getName %))))

(defn parse-semver [s]
  (when-let [[_ & parts] (re-matches #"(\d+)\.(\d+)\.(\d+)" s)]
    (mapv parse-long parts)))

(defn versions
  "Version folders of a role or skill directory, lowest first."
  [dir]
  (->> (subdirs dir)
       (filter #(parse-semver (.getName %)))
       (sort-by #(parse-semver (.getName %)))))

(defn frontmatter [text]
  (second (re-find #"(?s)\A---\n(.*?)\n---\n" text)))

(defn field [front k]
  (some-> (re-find (re-pattern (str "(?m)^" k ":\\s*(.+)$")) (or front "")) second str/trim))

(defn check-role-version [role-name dir]
  (let [label (str "roles/" role-name "/" (.getName dir))
        f     (io/file dir "role.edn")
        errs  (atom [])]
    (if-not (.exists f)
      (swap! errs conj "missing role.edn")
      (let [role (edn/read-string (slurp f))]
        (when-not (m/validate schema role)
          (swap! errs conj (str "schema: " (pr-str (me/humanize (m/explain schema role))))))
        (when-not (= role-name (some-> (:role/name role) name))
          (swap! errs conj (str ":role/name " (:role/name role) " does not match directory")))
        (when-let [h (:role/hands-to role)]
          (when-not (.exists (io/file "roles" (name h)))
            (swap! errs conj (str ":role/hands-to " h " is not a role"))))))
    (when-not (.exists (io/file dir "instructions.md"))
      (swap! errs conj "missing instructions.md"))
    (doseq [v (.listFiles dir)
            :let [[_ agent] (re-matches #"instructions-(.+)\.md" (.getName v))]
            :when (and agent (not (known-agents agent)))]
      (swap! errs conj (str "instructions variant for unknown agent " agent)))
    (mapv #(str label ": " %) @errs)))

(defn check-skill-version [skill-name dir]
  (let [label (str "skills/" skill-name "/" (.getName dir))
        f     (io/file dir "SKILL.md")]
    (if-not (.exists f)
      [(str label ": missing SKILL.md")]
      (let [front (frontmatter (slurp f))]
        (cond-> []
          (nil? front)                             (conj (str label ": missing frontmatter"))
          (not= skill-name (field front "name"))   (conj (str label ": frontmatter name does not match directory"))
          (str/blank? (field front "description")) (conj (str label ": missing description")))))))

(defn link-target [path]
  (let [p (.toPath (io/file path))]
    (when (java.nio.file.Files/isSymbolicLink p)
      (str (java.nio.file.Files/readSymbolicLink p)))))

(defn check-links
  "The root files of a role or skill must be symlinks to the highest version."
  [dir files]
  (let [latest (last (versions dir))]
    (when latest
      (for [file files
            :when (.exists (io/file latest file))
            :let [path     (str dir "/" file)
                  expected (str (.getName latest) "/" file)
                  actual   (link-target path)]
            :when (not= expected actual)]
        (str path ": should link to " expected (if actual (str ", links to " actual) ", is not a link"))))))

(defn check-component [kind link-files check-version dir]
  (let [name (.getName dir)
        vs   (versions dir)]
    (concat (when (empty? vs) [(str kind "/" name ": no version folder")])
            (mapcat #(check-version name %) vs)
            (check-links dir link-files))))

;; Immutability: a file in a version folder at the previous release tag must be unchanged.
;; Removing a whole version is allowed, since deprecated versions are removed in a later release.

(defn previous-tag []
  (let [{:keys [exit out]} (sh/sh "git" "describe" "--tags" "--abbrev=0" "--match" "v*")]
    (when (zero? exit) (str/trim out))))

(defn tag-files [tag]
  (let [{:keys [exit out]} (sh/sh "git" "ls-tree" "-r" "--name-only" tag "roles" "skills")]
    (if (zero? exit) (str/split-lines out) [])))

(defn git-show [tag path]
  (let [{:keys [exit out]} (sh/sh "git" "show" (str tag ":" path))]
    (when (zero? exit) out)))

(defn check-immutable [tag]
  (for [path (tag-files tag)
        :when (re-matches #"(roles|skills)/[^/]+/\d+\.\d+\.\d+/.+" path)
        :let [file (io/file path)]
        :when (and (.exists file) (not= (git-show tag path) (slurp file)))]
    (str path ": released in " tag " and must not change; add a new version folder instead")))

(let [tag    (previous-tag)
      roles  (subdirs "roles")
      skills (subdirs "skills")
      errors (vec (concat (mapcat #(check-component "roles" ["role.edn" "instructions.md"] check-role-version %) roles)
                          (mapcat #(check-component "skills" ["SKILL.md"] check-skill-version %) skills)
                          (when tag (check-immutable tag))))]
  (when-not tag
    (println "note: no release tag found, skipping the immutability check"))
  (if (seq errors)
    (do (doseq [e errors] (println "FAIL" e))
        (System/exit 1))
    (println "ok:" (count roles) "roles," (count skills) "skills"
             (if tag (str "(released versions unchanged since " tag ")") "(immutability not checked)"))))
