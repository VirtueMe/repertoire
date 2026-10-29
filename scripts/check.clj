;; CI check for the catalogue. Run with: bb check (Malli comes from bb.edn).
(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[malli.core :as m]
         '[malli.error :as me])

(def schema (m/schema (edn/read-string (slurp "schema/role.edn"))))

(defn subdirs [path]
  (->> (.listFiles (io/file path))
       (filter #(.isDirectory %))
       (sort-by #(.getName %))))

(defn check-role [dir]
  (let [dname (.getName dir)
        f     (io/file dir "role.edn")
        errs  (atom [])]
    (if-not (.exists f)
      (swap! errs conj "missing role.edn")
      (let [role (edn/read-string (slurp f))]
        (when-not (m/validate schema role)
          (swap! errs conj (str "schema: " (pr-str (me/humanize (m/explain schema role))))))
        (when-not (= dname (some-> (:role/name role) name))
          (swap! errs conj (str ":role/name " (:role/name role) " does not match directory")))
        (when-let [h (:role/hands-to role)]
          (when-not (.exists (io/file "roles" (name h)))
            (swap! errs conj (str ":role/hands-to " h " is not a role"))))))
    (when-not (.exists (io/file dir "instructions.md"))
      (swap! errs conj "missing instructions.md"))
    (mapv #(str "roles/" dname ": " %) @errs)))

(defn check-skill [dir]
  (let [dname (.getName dir)
        f     (io/file dir "SKILL.md")]
    (if-not (.exists f)
      [(str "skills/" dname ": missing SKILL.md")]
      (let [text (slurp f)
            front (second (re-find #"(?s)\A---\n(.*?)\n---\n" text))
            field (fn [k] (some-> (re-find (re-pattern (str "(?m)^" k ":\\s*(.+)$")) (or front "")) second str/trim))]
        (cond-> []
          (nil? front)                 (conj (str "skills/" dname ": missing frontmatter"))
          (not= dname (field "name"))  (conj (str "skills/" dname ": frontmatter name does not match directory"))
          (str/blank? (field "description")) (conj (str "skills/" dname ": missing description")))))))

(let [errors (vec (concat (mapcat check-role (subdirs "roles"))
                          (mapcat check-skill (subdirs "skills"))))]
  (if (seq errors)
    (do (doseq [e errors] (println "FAIL" e))
        (System/exit 1))
    (println "ok:" (count (subdirs "roles")) "roles," (count (subdirs "skills")) "skills")))
