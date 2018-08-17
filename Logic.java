package com.oblivionburn.realai;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Logic 
{
	//Variables
	public static String str_Input = "";
	public static String str_Output = "";
	public static String str_response = "";
	public static String str_last_response = "";
	public static String str_History = "";
	public static String[] WordArray;
	public static Boolean bl_Initiation = false;
	public static Boolean bl_NewInput = false;
	public static Boolean bl_MatchFound = false;
	private static List<String> Words = new ArrayList<String>();
	private static List<Integer> Frequencies = new ArrayList<Integer>();
	
	public static void prepInput() throws IOException
    {    	
    	if (!str_Input.equals(null) && !str_Input.equals(""))
    	{
    		try
    		{
    			char capital_letter = str_Input.charAt(0);
    			if (!Character.isUpperCase(capital_letter))
                {
    				String str_capital_letter = Character.toString(capital_letter);
                    str_capital_letter = str_capital_letter.toUpperCase(Locale.US);
                    
                    String str = str_Input;
                    StringBuilder sb = new StringBuilder(str).delete(0, 1);
                    sb.insert(0, str_capital_letter);
                    str_Input = sb.toString();
                }
    			
    			if (str_Input.charAt(str_Input.length() - 1) != '.')
        		{
    				str_Input += " .";
        		}
    			else if (str_Input.charAt(str_Input.length() - 1) == '.' && str_Input.charAt(str_Input.length() - 2) != ' ')
        		{
        			String str = str_Input;
                	StringBuilder sb = new StringBuilder(str);
                	sb.replace(str.indexOf('.'), str.indexOf('.') + 1, " .");
                	str_Input = sb.toString();
        		}
	    		
	    		WordArray = str_Input.split(" ");
	    		
	    		for (int i = 0; i < WordArray.length; i++)
	            {
	                if (WordArray[i].equals("."))
	                {
	                	WordArray[i] = " .";
	                }
	            }
	    		
	    		//Get current words
	    		Data.getWords();
	    		
	    		//Update the frequency of existing words
	    		if (Data.getWordDataSet().size() > 0)
	    		{
	    			for (int a = 0; a < Data.getWordDataSet().size(); a++)
	                {
	                    for (int b = 0; b < WordArray.length; b++)
	                    {
	                        if (Data.getWordDataSet().get(a).getWord().equals(WordArray[b]))
	                        {
	                        	Data.getWordDataSet().get(a).setFrequency(Data.getWordDataSet().get(a).getFrequency() + 1);
	                        }
	                    }
	                }
	    			
	    			Data.saveWords();
	    		}
	    		
	    		//Add new words
	    		for (int i = 0; i < WordArray.length; i++)
	            {
	                if (Data.Words.contains(WordArray[i]))
	                {
	
	                }
	                else
	                {
	                	if (!WordArray[i].equals(""))
	                	{
	                		Data.getWords();
		                    WordData new_wordset = new WordData();
		                    new_wordset.setWord(WordArray[i]);
		                    new_wordset.setFrequency(1);
		                    Data.getWordDataSet().add(new_wordset);
		                    Data.saveWords();
		
		                    Data.getWordDataSet().clear();
		                    Data.savePreWords(WordArray[i]);
		                    Data.saveProWords(WordArray[i]);
	                	}
	                }
	            }
	    		
	    		//Check each word in the sentence after we have possibly created new pre/pro word tables in the previous code
	    		for (int i = 0; i < WordArray.length - 1; i++)
                {
                    //Get current pre_words from the database
	    			Data.getPreWords(WordArray[i + 1]);
	    			Data.Words.clear();

                    for (int a = 0; a < Data.getWordDataSet().size(); a++)
                    {
                    	Data.Words.add(Data.getWordDataSet().get(a).getWord());
                    }

                    //Update the frequency of existing words
                    if (Data.Words.contains(WordArray[i]))
                    {
                        int index = Data.Words.indexOf(WordArray[i]);
                        Data.getWordDataSet().get(index).setFrequency(Data.getWordDataSet().get(index).getFrequency() + 1);
                        Data.savePreWords(WordArray[i + 1]);
                    }
                    else
                    {
                        //Or add the word
                    	if (!WordArray[i].equals(""))
                    	{
                    		WordData new_wordset = new WordData();
                            new_wordset.setWord(WordArray[i]);
    	                    new_wordset.setFrequency(1);
    	                    Data.getWordDataSet().add(new_wordset);
    	                    Data.savePreWords(WordArray[i + 1]);
                    	}
                    }
                    
                    if (i == WordArray.length)
                    {

                    }
                    else
                    {
                        //Get current pro_words from the database
                    	Data.getWordDataSet().clear();
                    	Data.getProWords(WordArray[i]);
                    	Data.Words.clear();

                        for (int b = 0; b < Data.getWordDataSet().size(); b++)
                        {
                        	Data.Words.add(Data.getWordDataSet().get(b).getWord());
                        }

                        //Update the frequency of existing words
                        if (Data.Words.contains(WordArray[i + 1]))
                        {
                            int index = Data.Words.indexOf(WordArray[i + 1]);
                            Data.getWordDataSet().get(index).setFrequency(Data.getWordDataSet().get(index).getFrequency() + 1);
                            Data.saveProWords(WordArray[i]);
                        }
                        else
                        {
                            //Or add the word
                        	if (!WordArray[i + 1].equals(""))
                        	{
                        		WordData new_wordset = new WordData();
                                new_wordset.setWord(WordArray[i + 1]);
        	                    new_wordset.setFrequency(1);
        	                    Data.getWordDataSet().add(new_wordset);
        	                    Data.saveProWords(WordArray[i]);
                        	}
                        }
                    }
                }
    		}
    		catch(IOException ex)
    		{
    			ex.printStackTrace();
    		}
    	}
    }

    public static void Respond() throws FileNotFoundException
    {
    	//Get the lowest frequency word from the input
    	Frequencies.clear();
    	Words.clear();
    	Data.getWords();
        String str_lowest_word = "";
        int int_lowest_f = 0;
        int int_highest_f = 0;
        
        if (bl_Initiation == true)
        {
            for (int a = 0; a < Data.getWordDataSet().size(); a++)
            {
            	Words.add(Data.getWordDataSet().get(a).getWord());
            	Frequencies.add(Data.getWordDataSet().get(a).getFrequency());
            }
            
            if (Words.size() > 0)
            {
            	Boolean bl_accepted = false;
                for (int i = 0; i < Words.size(); i++)
                {
	                Random random = new Random();
	                int int_choice = random.nextInt(Words.size());
	                str_lowest_word = Words.get(int_choice);
	                
	                if (str_lowest_word.equals("."))
                    {
                    	bl_accepted = false;
                    }
	                else
                    {
                    	bl_accepted = true;
                    }
                    
                    if (bl_accepted == true)
                    {
                    	break;
                    }
                }
            }
        }
        else
        {
        	if (WordArray != null)
            {
            	for (int a = 0; a < WordArray.length; a++)
                {
                    for (int a2 = 0; a2 < Data.getWordDataSet().size(); a2++)
                    {
                        if (Data.getWordDataSet().get(a2).getWord().equals(WordArray[a]))
                        {
                        	Words.add(Data.getWordDataSet().get(a2).getWord());
                        	Frequencies.add(Data.getWordDataSet().get(a2).getFrequency());
                        }
                    }
                }
            }
            
            if (Frequencies.size() > 0)
            {
            	int_lowest_f = GetMin(Frequencies);
            	List<Integer> RandomOnes = new ArrayList<Integer>();
                for (int b = 0; b < Frequencies.size(); b++)
                {
                    if (Frequencies.get(b) == int_lowest_f)
                    {
                        RandomOnes.add(b);
                    }
                }
                
                Boolean bl_accepted = false;
                for (int i = 0; i < RandomOnes.size(); i++)
                {
                	Random random = new Random();
                    int int_choice = random.nextInt(RandomOnes.size());
                    str_lowest_word = Words.get(RandomOnes.get(int_choice));
                    
                    if (str_lowest_word.equals("."))
                    {
                    	bl_accepted = false;
                    }
                    else
                    {
                    	bl_accepted = true;
                    }
                    
                    if (bl_accepted == true)
                    {
                    	break;
                    }
                }
                
            }
        }
        
        //Generate Response
        if (str_lowest_word.length() > 0)
        {
        	String str_current_pre_word = str_lowest_word;
            String str_current_pro_word = str_lowest_word;
            str_response = str_current_pre_word;
            Boolean bl_words_found = true;
            String[] arr_checker = null;
            String[] arr_checker2 = null;
            String str_repeater_check = "";

            if (bl_NewInput == true)
            {
            	if (str_last_response.length() > 1)
            	{                    
            		if (str_last_response.charAt(str_last_response.length() - 1) == '.' && str_last_response.charAt(str_last_response.length() - 2) != ' ')
            		{
            			String str = str_last_response;
                    	StringBuilder sb = new StringBuilder(str);
                    	sb.replace(str.indexOf('.'), str.indexOf('.') + 1, " .");
                    	str_last_response = sb.toString();
            		}
            	}
                
                Data.getOutputList(str_last_response);

                if (str_Input.length() > 1)
                {                    
                    if (str_Input.contains(".") && str_Input.charAt(str_Input.indexOf(".") - 1) != ' ')
                    {
                    	String str = str_Input;
                    	StringBuilder sb = new StringBuilder(str);
                    	sb.replace(str.indexOf('.'), str.indexOf('.') + 1, " .");
                    	str_Input = sb.toString();
                    }
                }
                
                if (Data.OutputList.contains(str_Input) || str_last_response.equals(str_Input))
                {

                }
                else
                {
                	Data.OutputList.add(str_Input);
                	Data.saveOutput(str_last_response);
                	
                	Data.getInputList();
                	if (!Data.InputList.contains(str_last_response))
                	{
                		Data.InputList.add(str_last_response);
                		try 
                		{
							Data.saveInputList();
						}
                		catch (IOException e)
                		{
							e.printStackTrace();
						}
                	}
                }
            }
            
            bl_MatchFound = false;
            Random random = new Random();
            
            Data.pullInfo(str_lowest_word);
            if (Data.InformationBank.size() > 0)
            {
                Random rand = new Random();
                int int_random_choice = rand.nextInt(Data.InformationBank.size());
                str_response = Data.InformationBank.get(int_random_choice);
                
                RulesCheck();
                str_last_response = str_response;
                str_Output = str_response;
                if (MainActivity.bl_Feedback = true)
                {
                	str_Input = str_response;
                }
                
                bl_NewInput = true;
                bl_MatchFound = true;
            }
            
            if (bl_MatchFound == false)
            {
            	Data.getInputList();
            	
            	if (str_Input.length() > 1)
            	{
                    if (str_Input.contains(".") && str_Input.charAt(str_Input.indexOf(".") - 1) != ' ')
                    {
                    	String str = str_Input;
                    	StringBuilder sb = new StringBuilder(str);
                    	sb.replace(str.indexOf('.'), str.indexOf('.') + 1, " .");
                    	str_Input = sb.toString();
                    }
            	}
            	
                if (Data.InputList.contains(str_Input) || str_Input.equals(null))
                {

                }
                else
                {
                	Data.InputList.add(str_Input);
                    try 
                    {
                    	Data.saveInputList();
					}
                    catch (IOException e)
					{
                    	e.printStackTrace();
					}
                }
                
                while (bl_words_found == true)
                {
                	Data.getPreWords(str_current_pre_word);
                    if (Data.getWordDataSet().size() > 0)
                    {
                    	Data.Words.clear();
                    	Data.Frequencies.clear();
                        for (int c = 0; c < Data.getWordDataSet().size(); c++)
                        {
                        	Data.Words.add(Data.getWordDataSet().get(c).getWord());
                        	Data.Frequencies.add(Data.getWordDataSet().get(c).getFrequency());
                        }

                        int_highest_f = GetMax(Data.Frequencies);
                        List<Integer> RandomOnes = new ArrayList<Integer>();
                        for (int b = 0; b < Data.Frequencies.size(); b++)
                        {
                            if (Data.Frequencies.get(b) == int_highest_f)
                            {
                                RandomOnes.add(b);
                            }
                        }
                        random = new Random();
                        int int_choice2 = random.nextInt(RandomOnes.size());
                        str_current_pre_word = Data.Words.get(RandomOnes.get(int_choice2));

                        if (str_current_pre_word.length() > 1)
                        {
                        	StringBuilder sb = new StringBuilder(str_current_pre_word).delete(1, str_current_pre_word.length() - 1);
                        	char first_letter = sb.charAt(0);
                            if (Character.isUpperCase(first_letter))
                            {
                            	String str2 = str_response;
                            	StringBuilder sb2 = new StringBuilder(str2).insert(0, str_current_pre_word + " ");
                                str_response = sb2.toString();
                                break;
                            }
                        }

                        arr_checker2 = str_response.split(" ");
                        for (int a = 0; a < arr_checker2.length; a++)
                        {
                            if (arr_checker2[a].equals(str_current_pre_word))
                            {
                                bl_words_found = false;
                                break;
                            }
                        }

                        if (bl_words_found != false)
                        {
                        	String str = str_response;
                        	StringBuilder sb = new StringBuilder(str).insert(0, str_current_pre_word + " ");
                            str_response = sb.toString();
                        }
                    }
                    else
                    {
                        bl_words_found = false;
                    }
                }
                bl_words_found = true;
                
                while (bl_words_found == true)
                {
                	Data.getProWords(str_current_pro_word);
                    if (Data.getWordDataSet().size() > 0)
                    {
                    	Data.Words.clear();
                    	Data.Frequencies.clear();
                        for (int e = 0; e < Data.getWordDataSet().size(); e++)
                        {
                        	Data.Words.add(Data.getWordDataSet().get(e).getWord());
                        	Data.Frequencies.add(Data.getWordDataSet().get(e).getFrequency());
                        }

                        int_highest_f = GetMax(Data.Frequencies);
                        List<Integer> RandomOnes = new ArrayList<Integer>();
                        for (int b = 0; b < Data.Frequencies.size(); b++)
                        {
                            if (Data.Frequencies.get(b) == int_highest_f)
                            {
                                RandomOnes.add(b);
                            }
                        }
                        random = new Random();
                        int int_choice2 = random.nextInt(RandomOnes.size());
                        str_current_pro_word = Data.Words.get(RandomOnes.get(int_choice2));
                        
                        if (str_repeater_check.length() > 0)
                        {
                        	arr_checker = str_repeater_check.split(" ");
                            for (int a = 0; a < arr_checker.length; a++)
                            {
                                if (arr_checker[a].equals(str_current_pro_word))
                                {
                                    bl_words_found = false;
                                    break;
                                }
                            }
                        }

                        if (bl_words_found != false)
                        {
                        	String str = str_response;
                        	StringBuilder sb = new StringBuilder(str).insert(str_response.length(), " " + str_current_pro_word);
                            str_response = sb.toString();

                            String str2 = str_repeater_check;
                        	StringBuilder sb2 = new StringBuilder(str2).insert(str_repeater_check.length(), str_current_pro_word + " ");
                        	str_repeater_check = sb2.toString();

                            if (str_current_pro_word.equals(".") || str_current_pro_word.equals("$") || str_current_pro_word.equals("!"))
                            {
                                bl_words_found = false;
                                break;
                            }
                        }
                    }
                    else
                    {
                        bl_words_found = false;
                    }
                }
                
                RulesCheck();
                str_Output = str_response;
                str_last_response = str_response;
                if (str_response.equals("") || str_response.equals(null))
                {
                	str_response = "";
                }
                if (MainActivity.bl_Feedback = true)
                {
                	str_Input = str_response;
                }
                
                bl_NewInput = true;
                bl_words_found = true;
            }
        }
        else
        {
            str_Output = "";
        }
    }
    
    private static int GetMin(List<Integer> Integer_List)
    {
        int lowest_number = 0;
        int current_number = Integer_List.get(0);
        for (int b = 0; b < Integer_List.size(); b++)
        {
            if (current_number <= Integer_List.get(b))
            {
                lowest_number = current_number;
            }
            else
            {
                current_number = Integer_List.get(b);
            }
        }

        return lowest_number;
    }
    
    private static int GetMax(List<Integer> Integer_List)
    {
        int highest_number = 0;
        int current_number = Integer_List.get(0);
        for (int b = 0; b < Integer_List.size(); b++)
        {
            if (current_number >= Integer_List.get(b))
            {
                highest_number = current_number;
            }
            else
            {
                current_number = Integer_List.get(b);
            }
        }

        return highest_number;
    }
    
    public static void FeedbackLoop() throws IOException 
    {
    	if (MainActivity.bl_Feedback == false)
    	{
    		bl_Initiation = true;
        	bl_NewInput = false;
        	Respond();
        	prepInput();
        	bl_Initiation = false;
        	MainActivity.bl_Feedback = true;
    	}
    	    	
    	Respond();
    	ClearLeftovers();
    }
    
    public static void ClearLeftovers()
    {
    	File file = new File(MainActivity.Brain_dir, ".txt");
    	if (file.exists())
    	{
    		file.delete();
    	}
    }
    
    public static void RulesCheck()
    {   
    	if (str_response.length() > 1)
    	{
            //Make sure the first word is capitalized
            char first_letter = str_response.charAt(0);
            if (Character.isUpperCase(first_letter))
            {

            }
            else
            {            
                String str_capital_letter = Character.toString(first_letter);
                str_capital_letter = str_capital_letter.toUpperCase(Locale.US);
                String str2 = str_response;
                StringBuilder sb2 = new StringBuilder(str2).delete(0, 1);
                sb2.insert(0, str_capital_letter);
                str_response = sb2.toString();
            }
            
            //Remove any empty spaces at the end
            String str2 = str_response;
            StringBuilder sb2 = new StringBuilder(str2).delete(0, str_response.length() - 1);
        	char last_letter = sb2.charAt(0);
            String str_last_letter = Character.toString(last_letter);
            
            while (str_last_letter.equals(" "))
            {            
            	String str3 = str_response;
            	StringBuilder sb3 = new StringBuilder(str3).delete(str_response.length() - 1, str_response.length());
                str_response = sb3.toString();
                
                String str4 = str_response;
                StringBuilder sb4 = new StringBuilder(str4).delete(0, str_response.length() - 1);
                last_letter = sb4.charAt(0);
                
                str_last_letter = Character.toString(last_letter);
            }
            
            //Set an ending punctuation if one does not exist
            if (!str_last_letter.equals("."))
            {
            	String str3 = str_response;
            	StringBuilder sb3 = new StringBuilder(str3).insert(str_response.length(), ".");
                str_response = sb3.toString();
            }
            
            //Remove any spaces before ending punctuation
            while (str_response.contains(" ."))
            {
            	String str3 = str_response;
                StringBuilder sb3 = new StringBuilder(str3).replace(str_response.indexOf(" ."), str_response.indexOf(" .") + 2, ".");
                str_response = sb3.toString();
            }
    	}
    }
}
