package com.sinequanonsolutions.simulator;

import java.util.Random;
import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class App
{
    private static Random rand = new Random();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    static StringBuilder original;
    static StringBuilder copySeq;

    public static void main( String[] args )
    {
        int nucleotideLen = 150; // TODO: get these values from the args array
        int pctDiff = 3;

        // create a nucleotide sequence that once translated, will serve as the target
        StringBuilder targetSeq = getRandomNSequence(nucleotideLen);

        original = new StringBuilder(targetSeq.toString());
        System.out.println( "target nucleotide sequence: "+targetSeq );

        // create a copy of the target and make random changes
        copySeq = createDifferentSequence(targetSeq, pctDiff);
        System.out.println( "copied nucleotide sequence: "+copySeq);
        System.out.println( "similarity of nucleotide sequences: "+calculateNucleotideSimilarity(original,copySeq));

        System.out.println( "target amino acid sequence: "+translateNSeq( original ));
        System.out.println( "copied amino acid sequence: "+translateNSeq( copySeq ));
        System.out.println("similarity of amino acid sequences: "+calculateSimilarity(original,copySeq)+"%");
        simulate(324000000L); // 3 billion for overnight (3000000000L) - 5.4 billion for the weekend - 324 million (324000000L) for lunch hour, 324 million/hour
    }

    private static void simulate(long trials){
        int matches = 0;
        long startTime = System.currentTimeMillis();
        int copySeqLen = copySeq.length();
        StringBuilder originalCopy = copySeq;
        double totalAvgSimilarity = 0.0;
        double maxSimilarity = 0.0;

       for (int i = 0; i < trials; i++)
       {
           int generation = 1;
           int counter = 0;
           while (true)
           {
               mutate(copySeq, copySeqLen);
               if (copySeq.equals(original))
               {
                   // record statistics
                   //System.out.println("MATCH!!!");
                   matches++;
                   break;
               }
               else if (generation > 75)
               {

                   double similarity = calculateSimilarity(original, copySeq);
                   maxSimilarity = (similarity > maxSimilarity) ? similarity : maxSimilarity;
                   totalAvgSimilarity = (totalAvgSimilarity + similarity);
                   // System.out.println("mutated copy - trial " + i + ":   " + translateNSeq(copySeq) + " - similarity: " + similarity + "%");
                   copySeq = originalCopy;
                   break;
               }
               else
               {
                   generation++;
                   counter++;
               }

               if(counter > 100000000) // every 100 million iterations, print status
               {
                   Date date = new Date();
                   System.out.println("Trials completed: "+(generation/1000000)+" million, "+sdf.format(new Timestamp((date.getTime()))));
                   counter = 0;
               }
           }
       }
       long endTime = System.currentTimeMillis();
       float duration = (endTime - startTime) / 1000; // elapsed seconds

       System.out.println("Total matches: "+matches);
       System.out.println("Average similarity of amino acid sequences at abandonment: "+(totalAvgSimilarity/(trials))+"% ");
       System.out.println("Maximum similarity of amino acid sequences at abandonment: "+maxSimilarity+"% ");
       System.out.println("Total trials: "+trials);
       System.out.println("Elapsed time: "+duration+" seconds");
    }

    private static StringBuilder createDifferentSequence(StringBuilder target, int pctDiff)
    {
        StringBuilder sequence = target;
        int seqLen = sequence.length();

        int numberOfChanges = Math.round(( pctDiff / (float) 100 ) * seqLen );

        for( int i = 0; i < numberOfChanges; i++ )
        {
            sequence = mutate(sequence, seqLen);
        }

        return sequence;
    }

    private static double calculateNucleotideSimilarity(StringBuilder target, StringBuilder copy){
        double pctSimilarity = 0.0F;

        String targetStr = target.toString();
        String copyStr = copy.toString();
        int len = targetStr.length();

        char[] targetArr = targetStr.toCharArray();
        char[] copyArr = copyStr.toCharArray();

        int index = 0;
        int matchCount = 0;

        for (int i = 0; i < len; i++ ){
            if( targetArr[i] == copyArr[i]){
                matchCount++;
            }
        }

        pctSimilarity = (matchCount / (float) len) * 100;

        return round(pctSimilarity, 3);
    }


    private static double calculateSimilarity(StringBuilder target, StringBuilder copy){
        double pctSimilarity = 0.0F;

        String targetTranslation = translateNSeq(target).toString();
        String copyTranslation = translateNSeq(copy).toString();
        int len = targetTranslation.length();

        char[] targetArr = targetTranslation.toCharArray();
        char[] copyArr = copyTranslation.toCharArray();

        int index = 0;
        int matchCount = 0;

        for (int i = 0; i < len; i++ ){
            if( targetArr[i] == copyArr[i]){
                matchCount++;
            }
        }

        pctSimilarity = (matchCount / (float) len) * 100;

        return round(pctSimilarity, 3);
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    private static StringBuilder mutate(StringBuilder original, int seqLen)
    {
        StringBuilder mutant = original;

        // get random position
        int randPos = rand.nextInt(seqLen);

        // get string at that position
        String currentValue = String.valueOf(mutant.charAt(randPos));

        // get random nucleotide, ensure that it is not the same as the current value
        String randomNucleotide = null;
        while (true)
        {
            randomNucleotide = getRandomNucleotide();
            if(!currentValue.equals(randomNucleotide)){
                break;
            }
        }

        // make substitution
        mutant.setCharAt(randPos, randomNucleotide.charAt(0));

        return mutant;

    }

    private static String getRandomNucleotide(){
        String randomNucleotide = null;

        switch(rand.nextInt(4) + 1)
        {
            case 1:
                randomNucleotide = "A"; // Adenine
                break;
            case 2:
                randomNucleotide = "C"; // Cytosine
                break;
            case 3:
                randomNucleotide = "G"; // Guanine
                break;
            case 4:
                randomNucleotide = "U"; // Uracil
                break;
        }

        return randomNucleotide;
    }

    private static StringBuilder translateNSeq(StringBuilder source)
    {

        StringBuilder translation = new StringBuilder();
        int len = source.length();

        for( int i = 0; i < len; i+=3 )
        {
            String codon = source.substring(i,i+3);
            if (codon.length() == 3)
            {
                translation.append(translateNtoA(codon));
            }
        }

        return translation;
    }

    private static String translateNtoA(String source)
    {
        String translation = null;

        switch(source)
        {
            case "UUU":
            case "UUC":
                translation = "F";
                break;
            case "UUA":
            case "UUG":
            case "CUU":
            case "CUC":
            case "CUA":
            case "CUG":
                translation = "L";
                break;
            case "AUU":
            case "AUC":
            case "AUA":
                translation = "I";
                break;
            case "AUG":
                translation = "M";
                break;
            case "GUU":
            case "GUC":
            case "GUA":
            case "GUG":
                translation = "V";
                break;
            case "UCU":
            case "UCC":
            case "UCA":
            case "UCG":
            case "AGU":
            case "AGC":
                translation = "S";
                break;
            case "CCU":
            case "CCC":
            case "CCA":
            case "CCG":
                translation = "P";
                break;
            case "ACU":
            case "ACC":
            case "ACA":
            case "ACG":
                translation = "T";
                break;
            case "GCU":
            case "GCC":
            case "GCA":
            case "GCG":
                translation = "A";
                break;
            case "UAU":
            case "UAC":
                translation = "Y";
                break;
            case "UAA":
            case "UAG":
            case "UGA":
                translation = "*";
                break;
            case "CAU":
            case "CAC":
                translation = "H";
                break;
            case "CAA":
            case "CAG":
                translation = "Q";
                break;
            case "AAU":
            case "AAC":
                translation = "N";
                break;
            case "AAA":
            case "AAG":
                translation = "K";
                break;
            case "GAU":
            case "GAC":
                translation = "D";
                break;
            case "GAA":
            case "GAG":
                translation = "E";
                break;
            case "UGU":
            case "UGC":
                translation = "C";
                break;
            case "UGG":
                translation = "W";
                break;
            case "CGU":
            case "CGC":
            case "CGA":
            case "CGG":
            case "AGA":
            case "AGG":
                translation = "R";
                break;
            case "GGU":
            case "GGC":
            case "GGA":
            case "GGG":
                translation = "G";
                break;
        }

        return translation;
    }

    private static StringBuilder getRandomNSequence(int length)
    {
        StringBuilder nSeq = new StringBuilder();

        for( int i = 0; i < length; i++)
        {
            nSeq.append(getRandomNucleotide());
        }

        return nSeq;
    }

    private static StringBuilder getRandomAASequence(int length)
    {
        StringBuilder aaSeq = new StringBuilder();

        for( int i = 0; i < length; i++ )
        {
            switch(rand.nextInt(20) + 1)
            {
                case 1:
                    aaSeq.append("A"); // Alanine
                    break;
                case 2:
                    aaSeq.append("C"); // Cysteine
                    break;
                case 3:
                    aaSeq.append("D"); // Aspartic acid
                    break;
                case 4:
                    aaSeq.append("E"); // Glutamic acid
                    break;
                case 5:
                    aaSeq.append("F"); // Phenylalanine
                    break;
                case 6:
                    aaSeq.append("G"); // Glycine
                    break;
                case 7:
                    aaSeq.append("H"); // Histidine
                    break;
                case 8:
                    aaSeq.append("I"); // Isoleucine
                    break;
                case 9:
                    aaSeq.append("K"); // Lysine
                    break;
                case 10:
                    aaSeq.append("L"); // Leucine
                    break;
                case 11:
                    aaSeq.append("M"); // Methionine
                    break;
                case 12:
                    aaSeq.append("N"); // Asparagine
                    break;
                case 13:
                    aaSeq.append("P"); // Proline
                    break;
                case 14:
                    aaSeq.append("Q"); // Glutamine
                    break;
                case 15:
                    aaSeq.append("R"); // Arginine
                    break;
                case 16:
                    aaSeq.append("S"); // Serine
                    break;
                case 17:
                    aaSeq.append("T"); // Threonine
                    break;
                case 18:
                    aaSeq.append("V"); // Valine
                    break;
                case 19:
                    aaSeq.append("W"); // Tryptophan
                    break;
                case 20:
                    aaSeq.append("Y"); // Tyrosine
                    break;
            }
        }

        return aaSeq;
    }
}
