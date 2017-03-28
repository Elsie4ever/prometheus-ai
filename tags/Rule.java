package tags;

import java.util.Arrays;

/**
 * Represents a rule in the expert system.
 */
public class Rule extends Tag {
    public Tag[] inputTags;
    public Tag[] outputTags;

    /**
     * Creates a Rule.
     *
     * @param inputTags condition tags of the rule.
     * @param outputTag outputTags tag of the rule.
     */
    public Rule(Tag[] inputTags, Tag[] outputTag) {
        this.inputTags = inputTags;
        this.outputTags = outputTag;
        this.type = Type.RULE;
        this.value = this.toString();
    }


    /**
     * Creates a Rule from Strings, assuming all Tags (input and output) are of the provided Type.
     *
     * @param inputTags  the input Tags, in String form.
     * @param outputTags the output Tags, in String form.
     * @param type       the Type of both input and output Tags.
     */
    public Rule(String[] inputTags, String[] outputTags, Type type) {
        int m = inputTags.length;
        int n = outputTags.length;
        this.inputTags = new Tag[m];
        this.outputTags = new Tag[n];
        for (int i = 0; i < m; i++) {
            this.inputTags[i] = Tag.createTagFromString(inputTags[i], type);
        }
        for (int i = 0; i < n; i++) {
            this.outputTags[i] = Tag.createTagFromString(outputTags[i], type);
        }
        this.type = Type.RULE;
        this.value = this.toString();
    }

    /**
     * Creates a Rule from a single String.
     * TODO: Parse the String to produce a Rule.
     *
     * @param value the String representing the Rule.
     */
    public Rule(String value) {
        super(value, Type.RULE);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Rule rule = (Rule) o;
        return Arrays.equals(inputTags, rule.inputTags) && Arrays.equals(outputTags, rule.outputTags);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(inputTags);
        result = 31 * result + Arrays.hashCode(outputTags);
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(inputTags) + "=>" + Arrays.toString(outputTags);
    }
}
