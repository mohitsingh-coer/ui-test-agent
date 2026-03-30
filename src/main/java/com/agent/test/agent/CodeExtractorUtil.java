package com.agent.test.agent;

public class CodeExtractorUtil {
    public static String extractCodeBlock(String llmOutput, String language) {
        String startTag = "```" + language;
        String endTag = "```";

        int startIndex = llmOutput.toLowerCase().indexOf(startTag);
        if (startIndex != -1) {
            startIndex += startTag.length();
            int endIndex = llmOutput.indexOf(endTag, startIndex);
            if (endIndex != -1) {
                return llmOutput.substring(startIndex, endIndex).trim();
            }
        }
        
        // If specific language tag is not found, try generic java or gherkin fallback, 
        // or just plain empty tag if it exists.
        startIndex = llmOutput.indexOf("```");
        if(startIndex != -1) {
             // Find end of the first line (past the ```` language identifier)
             int eolIndex = llmOutput.indexOf("\n", startIndex);
             if(eolIndex != -1) {
                 int endIndex = llmOutput.indexOf(endTag, eolIndex);
                 if(endIndex != -1) {
                     return llmOutput.substring(eolIndex, endIndex).trim();
                 }
             }
        }
        
        // Fallback: Return raw output if no code block tags found
        return llmOutput.trim();
    }
}
