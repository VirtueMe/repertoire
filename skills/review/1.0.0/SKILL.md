---
name: review
description: Review a diff for simplicity, correctness and fit with ADRs, through the lens of complecting versus simplicity. Produces findings and concrete recommendations; changes no files. Use at the end of a non-trivial batch, or when asked to review a branch.
---

# Review: simplicity on the diff

Keep the two phases apart: first interrogate, then recommend.

## Skip rule

Skip trivial batches: about 20 lines or fewer, a single file or docs only, and no new abstraction, state, dependency or public interface. Say that the review was skipped.

## Read

```bash
git diff --staged
git diff
```

If nothing is staged, review the unstaged diff. If the diff is empty, say so and stop. Read the surrounding files, not just the deltas; surprises hide in small diffs.

## Interrogate

Take each question in its own pass, with the skeptical, precise stance of someone who distrusts familiarity:

1. **Complected?** What is braided together that should stay separate? Name both strands: state and identity, time and value, data and behaviour, interface and implementation. Say whether the braid is load-bearing or incidental.
2. **Simple, or familiar?** Would a reader with no context agree it is simple?
3. **Easy mistaken for simple?** A convenient API on a complected core is easy, not simple.
4. **New state, coupling or indirection?** Each is a claim that needs justification.
5. **Cold in two years?** Without the ticket and this conversation, does it explain itself?
6. **Correct and consistent?** Does it meet the contract and fit the ADRs?

Quote the diff when it matters. Stop when you have seen enough: some diffs have one finding, some none. No compliments beyond one sincere sentence.

## Recommend

For each finding, say what to change (function, file, line range), why in one sentence, and the effort (trivial, small, real refactor). Order by payoff. If nothing is worth changing, say so plainly.

## Close

End with exactly one of:

- **Ready to commit.** The diff is clean or the remainder is cosmetic.
- **Fix the above, then commit.** The recommendations are material.
- **Consider the above before committing; the author's call.** The findings are genuine but subjective.

## Never

- Change files, create commits or alter git state. You only read.
- Review work you produced yourself.
