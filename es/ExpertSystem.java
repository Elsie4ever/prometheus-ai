package es;

import interfaces.PrometheusLayer;
import tags.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Expert System (ES)
 */
public class ExpertSystem implements PrometheusLayer {
    private Set<Rule> readyRules;
    private Set<Rule> activeRules;
    private Set<Fact> facts;
    private Set<Recommendation> recommendations;
    public HashMap<String, Argument> pendingReplacementPairs;


    /**
     * Creates an Expert System (ES).
     */
    public ExpertSystem() {
        readyRules = new HashSet<>();
        facts = new HashSet<>();
        recommendations = new HashSet<>();
        activeRules = new HashSet<>();
        pendingReplacementPairs = new HashMap<>();
    }

    /**
     * Resets the ES by clearing all Rules, Recommendations, and Facts.
     */
    public void reset() {
        activeRules.clear();
        readyRules.clear();
        facts.clear();
        recommendations.clear();
    }

    /**
     * Deactivates all active Rules.
     */
    public void deactivateRules() {
        readyRules.addAll(activeRules);
        activeRules.clear();
    }

    /**
     * Adds a Tag to the ES. Will cast the tag to either a Rule, a Fact, or a Recommendation.
     *
     * @param tag the Tag to be added
     * @return <code>true</code> if the Tag is successfully added
     */
    public boolean addTag(Tag tag) {
        switch (tag.type) {
            case RULE:
                return addRule((Rule) tag);
            case FACT:
                return addFact((Fact) tag);
            case RECOMMENDATION:
                return addRecommendation((Recommendation) tag);
        }
        return false;
    }

    /**
     * Adds multiple Tags to the ES.
     *
     * @param tags the Tags to be added
     * @return <code>true</code> if all the Tags are added successfully
     */
    public boolean addTags(Set<Tag> tags) {
        boolean allAdded = true;
        for (Tag t : tags) {
            if (!addTag(t)) allAdded = false;
        }
        return allAdded;
    }

    /**
     * Adds a Fact to the ES.
     *
     * @param fact the Fact to be added
     * @return <code>true</code> if the ES did not already contain the specified Fact
     */
    public boolean addFact(Fact fact) {
        return facts.add(fact);
    }

    public boolean removeFact(Fact fact) {
        return facts.remove(fact);
    }

    /**
     * Adds a Rule to the ES.
     *
     * @param rule the Rule to be added
     * @return <code>true</code> if the ES did not already contain the specified Rule
     */
    public boolean addRule(Rule rule) {
        return readyRules.add(rule);
    }

    /**
     * Adds a Recommendation to the ES.
     *
     * @param rec the Recommendation to be added
     * @return <code>true</code> if the ES did not already contain the specified Recommendation
     */
    public boolean addRecommendation(Recommendation rec) {
        return recommendations.add(rec);
    }

    /**
     * Gets all the Recommendations of the ES.
     *
     * @return the Recommendations of the ES
     */
    public Set<Recommendation> getRecommendations() {
        return recommendations;
    }

    /**
     * Gets the ready Rules of the ES.
     *
     * @return the ready Rules of the ES
     */
    public Set<Rule> getReadyRules() { // For testing purposes
        return readyRules;
    }

    /**
     * Gets the active Rules of the ES.
     *
     * @return the active Rules of the ES
     */
    public Set<Rule> getActiveRules() { // For testing purposes
        return activeRules;
    }

    /**
     * Gets the Facts of the ES.
     *
     * @return the Facts of the ES
     */
    public Set<Fact> getFacts() { // For testing purposes
        return facts;
    }

    public boolean factsContains(Fact inputFact) {
        boolean result = false;
        for (Fact f : facts) {
            VariableReturn matchesResult = f.matches(inputFact);
            if (matchesResult.doesMatch) {
                if (matchesResult.pairs.size() > 0) {
                    pendingReplacementPairs.putAll(matchesResult.pairs);
                }
                result = true;
            }
        }
        return result;
    }

    /**
     * Continuously iterates through the read Rules, checking Facts and Recommendations, and activating Rules if
     * possible. Stops once the system reaches natural quiescence.
     *
     * @return the activated Recommendations as a result of thinking
     */
    public Set<Tag> think() {
        Set<Tag> allActivatedTags = new HashSet<>();
        Set<Tag> activatedTags;
        Set<Fact> inputFactSet = getFacts();
        do {
            activatedTags = thinkCycle();
            allActivatedTags.addAll(activatedTags);
        } while (!activatedTags.isEmpty());
        Set<Tag> activatedRecommendations = new HashSet<>();
        for (Tag tag : allActivatedTags) {
            if (tag.isRecommendation())
                activatedRecommendations.add(tag);
        }
        generateProvenRule(allActivatedTags, inputFactSet);
        return activatedRecommendations;
    }

    private void generateProvenRule(Set<Tag> allActivatedTags, Set<Fact> inputFactSet) {
        Fact[] inputFacts = inputFactSet.toArray(new Fact[inputFactSet.size()]);
        Tag[] outputTags = allActivatedTags.toArray(new Tag[allActivatedTags.size()]);
        Rule provenRule = new Rule(inputFacts, (Fact[]) outputTags);
        addRule(provenRule);
    }

    /**
     * Makes the ES think for a fixed number of cycles. The number of cycles represents how much effort is being put
     * into thinking. Each cycle is a run-through of all the ready Rules, activating Rules if possible. Note that a Rule
     * that is activated in a cycle is not iterated over in that same cycle, and must wait until the next cycle to
     * cascade further activation. This is threshold quiescence, which may or may not correspond with natural
     * quiescence.
     *
     * @param numberOfCycles the number of cycles to think for
     * @return the activated Recommendations as a result of thinking
     */
    public Set<Tag> think(int numberOfCycles) {
        Set<Tag> allActivatedTags = new HashSet<>();
        for (int i = 0; i < numberOfCycles; i++) {
            Set<Tag> activatedTags = thinkCycle();
            if (activatedTags.isEmpty())
                break;
            allActivatedTags.addAll(activatedTags);
        }
        Set<Tag> activatedRecommendations = new HashSet<>();
        for (Tag tag : allActivatedTags) {
            if (tag.isRecommendation())
                activatedRecommendations.add(tag);
        }
        return activatedRecommendations;
    }

    /**
     * Makes the ES think for a single cycle.
     *
     * @return the activated Tags as a result of thinking
     */
    private Set<Tag> thinkCycle() {
        Set<Tag> activatedTags = new HashSet<>();
        Set<Rule> pendingActivatedRules = new HashSet<>();
        for (Rule rule : readyRules) {
            boolean shouldActivate = true;
            for (Fact fact : rule.inputFacts) {
                if (!factsContains(fact)) {
                    shouldActivate = false;
                    break;
                }
            }
            if (shouldActivate) {
                pendingActivatedRules.add(rule);
            }
        }
        for (Rule rule : pendingActivatedRules) {
            readyRules.remove(rule);
            activeRules.add(rule);
            for (Tag tag : rule.outputTags) {
                replaceVariableArguments((Fact) tag);
                if (tag.type.equals(Tag.TagType.FACT) && !factsContains((Fact) tag)) {
                    activatedTags.add(tag);
                    addTag(tag);
                } else if (tag.type.equals(Tag.TagType.RECOMMENDATION) && !recommendations.contains(tag)) {
                    activatedTags.add(tag);
                    addTag(tag);
                }
            }
        }
        return activatedTags;
    }

    //TODO: Variable matching when argument contains math symbol
    private void replaceVariableArguments(Fact tag) {
        int argumentIndex = 0;
        for (Argument argument : tag.getArguments()) {
            if (!pendingReplacementPairs.isEmpty() &&
                    pendingReplacementPairs.containsKey(argument.getName())) {
                LinkedList<Argument> newArguments = tag.getArguments();
                newArguments.set(argumentIndex, pendingReplacementPairs.get(argument.getName()));
                tag.setArguments(newArguments); //can be set all at once (NI)
            }
            argumentIndex++;
        }
    }

    private Rule logicReasoner(Rule inputRule, Rule outputRule) {
        for (Fact inputFact : inputRule.getOutputTags()) {
            boolean fullMatch = false;
            for (Fact outputFact : outputRule.getInputFacts()) {
                if (outputFact.matches(inputFact).doesMatch) {
                    fullMatch = true;
                    break;
                }
            }
            if (!fullMatch) {
                return new Rule();
            }
        }
        return new Rule(inputRule.inputFacts, outputRule.outputTags);
    }

    public HashSet<Rule> pairwiseComp(int numberOfCycles) {
        HashSet<Rule> mergedRules = new HashSet<>();
        HashSet<Rule> inputRules = new HashSet<>(this.readyRules);
        while (numberOfCycles > 0) {
            for (Rule ruleOne : inputRules) {
                for (Rule ruleTwo : inputRules) {
                    Rule mergedRule = logicReasoner(ruleOne, ruleTwo);
                    if (!mergedRule.equals(new Rule())) {
                        mergedRules.add(mergedRule);
                    }
                }
            }
            numberOfCycles--;
            inputRules = mergedRules;
        }
        return mergedRules;
    }

    public void rest() {
        HashSet<Rule> newRules = pairwiseComp(1);
        for (Rule newRule : newRules) {
            addRule(newRule);
        }
    }

}