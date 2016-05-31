import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
 import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
    /*
    * Hints in doing the HW:
    *   a) Make sure you first understand what you are doing.
    *   b) Watch Lecture 2 focusing on the code described
     */
    class homework3 
    { 
    	 static final class Index
         {
             Integer value;
             boolean fromVariable;
             boolean fromOtherArray;
             boolean fromRecord;
             int depth;
             public Index(Integer val, boolean needInd,boolean array,boolean record,int depth )
             {
                 this.value=val;
                 fromVariable=needInd;
                 fromOtherArray=array;
                 fromRecord=record;
                 this.depth=depth;
             }
         }
         // Abstract Syntax Tree
         static final class AST
         {
             public final String value;
             public final AST left; // can be null
             public final AST right; // can be null
  
             private AST(String val,AST left, AST right) 
             {
                 value = val;
                 this.left = left;
                 this.right = right;
             }
             public static AST createAST(Scanner input) 
             {
                 if (!input.hasNext())
                     return null;
  
                 String value = input.nextLine();
                 if (value.equals("~"))
                     return null;
                 
                 return new AST(value, createAST(input), createAST(input));
             }
         }
  
         static final class Variable
        {
             //general attributes of variables
            static int nextAvailableAddress=5;
            public String name;
            public int address;
            public String type;
            public int size;
            public String DefinedIn;
             
            //attributes of a pointer
            public String pointerToType;
            public String pointerToVariableName;
             
            //attributes for record
            public ArrayList<Variable> structMembers;
            public String fatherRecord;
            public int offset=0;
             
            //attributes for array
            public ArrayList<Integer> dimensionsSizes;
            public int subpart=0;
            public String cellType;
            public int cellSize;
            //atributes for array of array
            public String ArrayName;
             
            //attributes for func\proc
            public int absDepth;
            public ArrayList<Variable> paramList;
            public String returnType; 
            
            //attributes for variable in paramList
            public String byReferenceOrValue="";
            
            //cot'r for int/bool/real
            public Variable(String varName, String varType, String father,int order,String DefinedIn,String byReferenceOrValue,int depth)
            {
                this.name= varName;
                this.type=varType;
                offset=order;
                fatherRecord=father;
                structMembers=null;
                this.address=nextAvailableAddress;
                nextAvailableAddress++;
                this.size=1;
                this.DefinedIn=DefinedIn;
                this.byReferenceOrValue=byReferenceOrValue;
                this.absDepth=depth;
            }
            
            //cot'r for descriptor
            public Variable(String varName, String varType, String DefinedIn,String byReferenceOrValue, int depth)
            {
                this.name= varName;
                this.type=varType;
                structMembers=null;
                this.address=nextAvailableAddress;
                nextAvailableAddress+=2;
                this.size=2;
                this.DefinedIn=DefinedIn;
                this.absDepth=depth;
                this.byReferenceOrValue=byReferenceOrValue;
            }
            //cot'r for pointer
            public Variable(String varName, String varType,String pointedType,String father, int order,String pointerVarName,String DefinedIn,String byReferenceOrValue,int depth)
            {
                this.name= varName;
                this.type=varType;
                fatherRecord=father;
                structMembers=null;
                offset=order;
                this.address=nextAvailableAddress;
                nextAvailableAddress++;
                this.size=1;
                this.pointerToType=pointedType;
                this.pointerToVariableName=pointerVarName;
                this.DefinedIn=DefinedIn;
                this.byReferenceOrValue=byReferenceOrValue;
                this.absDepth=depth;
            }
            //cot'r for record
            public Variable(String varName, String varType, int size,String father,ArrayList<Variable> members,int order,int address,String DefinedIn,String byReferenceOrValue,int depth)
            {
                this.name= varName;
                this.type=varType;
                fatherRecord=father;
                structMembers=new ArrayList<Variable>();
                structMembers=members;
                offset=order;
                this.address=address;//same address as variable in offset 0 from him
                this.size=size;
                this.DefinedIn=DefinedIn;
                this.byReferenceOrValue=byReferenceOrValue;
                this.absDepth=depth;
            }
             
            //cot'r for array
            public Variable(String varName, String varType,String father,ArrayList<Integer> limits,String cellType, int cellSize,int order,String array,String DefinedIn,String byReferenceOrValue,int depth)
            {
                this.name= varName;
                this.address=nextAvailableAddress;
                this.type=varType;
                ArrayName=array;
                fatherRecord=father;
                this.size=cellSize;
                this.cellSize=cellSize;
                offset=order;
                this.dimensionsSizes=new ArrayList<Integer>();
                this.DefinedIn=DefinedIn;
                this.byReferenceOrValue=byReferenceOrValue;
                this.absDepth=depth;
                //calculating size from dimensions
                int i=0;
                int j=1;
                int k=1;
                while(i<limits.size()){
                    int dim= limits.get(i+1)-limits.get(i)+1;
                    this.size*= dim;
                    this.dimensionsSizes.add(dim);
                    i+=2;
                }
                
                //calculating subpart
                //i-limits array index,j-dimensions array index
                for(i=0,k=1; i < limits.size(); i+=2,k++)
                {
                	int mul=limits.get(i);
                    for(j=k; j< dimensionsSizes.size();j++ )
                    {
                        mul*=dimensionsSizes.get(j);
                    }
                    	subpart+=mul; //otherwise, adding the calculation
                }
                //eventually, multiply subpart by cell size
                subpart*=cellSize;
                nextAvailableAddress+=this.size;
            }
          
            //cot'r for function/procedure
            public Variable(String varName,String varType,String returnType ,String DefinedIn,String byReferenceOrValue,int depth )
            {
                this.name= varName;
                this.type=varType;
                this.returnType=returnType;
                this.absDepth=0;
                this.paramList=new ArrayList<Variable>();
                this.DefinedIn=DefinedIn; //papa
                this.byReferenceOrValue=byReferenceOrValue;
                this.address=0; //for return value "variable"
                this.absDepth=depth;
            }
        }
 
        static final class SymbolTable
        {
            // Think! what does a SymbolTable contain?
            public static HashMap<String,Variable> symbolTable=new HashMap<String,Variable>();
             
            public static ArrayList<Integer> arrayLimits=new ArrayList<Integer>();
             
            public static String innerMostStructFather=null;
            
            public static String papa;
            
           public static String papaKey="";
            
            public static int depth=0; 
             
            public static ArrayList<Integer> appearanceOrderForRecordSonPerStruct=new ArrayList<Integer>();
            
            private static String arrayInParamList;
            
            public static boolean isByReferenceFlag=false;
            
            public static void generateSymbolTable(AST tree)
            {
               if(tree == null)
                    return;
                 
                if(tree.left != null)
                {
                	if(tree.left.value.equals("declarationsList"))
                	{    //go down the tree as far left as possible to get to declaration 1
                		 SymbolTable.generateSymbolTable(tree.left);
                	}
                }
                
                if(tree.right != null){
                	if(tree.right.value.equals("functionsList")){
                		SymbolTable.generateSymbolTable(tree.right);
                	}
                }
                if(tree.value.equals("functionsList")){
                	//more than one function defined in the same depth
                	if( tree.left!=null)
                		SymbolTable.generateSymbolTable(tree.left);
                	//in any case there is at least one function/procedure defined to the right
                	SymbolTable.generateSymbolTable(tree.right);
                }
                //we are in function\procedure ->need to change papa value
               
                if(tree.value.equals("procedure") || tree.value.equals("function")){
                	//create new variable- name, type (func\proc),return value,defined in
                	String returnType="";
                	if(tree.value.equals("function"))
                	{                	
                		returnType=tree.left.right.right.value;
                	}
                	symbolTable.put(tree.left.left.left.value+"$" + papa,new Variable(tree.left.left.left.value,tree.value,returnType,papa,null,++depth));
                	papaKey=tree.left.left.left.value+"$" + papa;
                	papa=tree.left.left.left.value;
                	
                	Variable.nextAvailableAddress=5;
                	//recursive call to get param list
                	if(tree.left.right.left!=null)
                		generateSymbolTable(tree.left.right.left);
                	
                	//go right for funcs defined in this func
                	generateSymbolTable(tree.right.left);
                	depth--;
                }
                 if (tree.value.equals("parametersList"))
                 {
                	 if(tree.left!=null)
                	 {
                		 generateSymbolTable(tree.left); 
                	 }
                     //handle params - by reference or by value
                	 //first case (simple) - the param is int/real/bool
                	 if (tree.right.right.value.equals("int") ||(tree.right.right.value.equals("real")||(tree.right.right.value.equals("bool"))))
                			 {
                		 		symbolTable.put(tree.right.left.left.value+"$" + papa, new Variable(tree.right.left.left.value,tree.right.right.value,null,0,papa,tree.right.value,depth));
                		 		symbolTable.get(papaKey).paramList.add(symbolTable.get(tree.right.left.left.value+"$" + papa));
                			 }
                	 else if (tree.right.right.value.equals("array"))
                	 {
                		    arrayInParamList=tree.right.left.left.value;
                		    isByReferenceFlag=tree.right.value.equals("byReference");
                		 	generateSymbolTable(tree.right.right); 
                	 }
                	 else if (tree.right.right.value.equals("pointer"))
                	 {
                		  String identifierType;
                          String pointerVarName="";
                          switch (tree.right.right.left.value)//if pointer defined by another identifier need to figure out its type
                          {
                          case "identifier": //need to find the identifier type in symbol table and get its type
                               	 identifierType=SymbolTable.symbolTable.get(tree.right.right.left.left.value+"$" + papa).type;
                              	 pointerVarName=SymbolTable.symbolTable.get(tree.right.right.left.left.value+"$" + papa).name;
                                 break;
                          default:
                                identifierType=tree.right.right.left.value;
                                  break;
                          } 
                          SymbolTable.symbolTable.put(tree.right.left.left.value+"$" + papa,new Variable(tree.right.left.left.value,"pointer",identifierType,null,0,pointerVarName,papa,tree.right.value,depth));
                          symbolTable.get(papaKey).paramList.add(symbolTable.get(tree.right.left.left.value+"$" + papa));
                	 }
                	 else if( tree.right.right.value.equals("identifier"))
                	 {
                		 Variable param=SymbolTable.symbolTable.get(tree.right.right.left.value+"$" + papa);
                		 if(param.type.equals("function") || param.type.equals("function")){
                			 param =new Variable(tree.right.left.left.value,"descriptor",papa,tree.right.value,depth); 
                		 }
                		 else
                		 {
                			 param.name=tree.right.left.left.value;
                			 param.address=Variable.nextAvailableAddress;
                			 Variable.nextAvailableAddress+=param.size;
                			 param.byReferenceOrValue=tree.right.value;
                			 param.DefinedIn=papa;
                		 	}
                		 symbolTable.put(param.name+"$" + papa, param);
                		 symbolTable.get(papaKey).paramList.add(param);
                		 }
                     return;
                 }
            
                 //handling declarations list
                //pointer case-create pointer
                 if (tree.value.equals("scope")) return; //exit criteria - we have climbed back up to scope and no other conditions are met
                if (tree.right.right!=null &&tree.right.right.value.equalsIgnoreCase("pointer"))
                    {
                        String identifierType;
                        String pointerVarName="";
                        switch (tree.right.right.left.value)//if pointer defined by another identifier need to figure out its type
                        {
                        case "identifier": //need to find the identifier type in symbol table and get its type
                            if (SymbolTable.symbolTable.get(tree.right.right.left.left.value+"$" + papa)==null) {
                                identifierType="record";
                                pointerVarName=tree.right.right.left.left.value;
                            }
                             else {
                            	 identifierType=SymbolTable.symbolTable.get(tree.right.right.left.left.value+"$" + papa).type;
                            	 pointerVarName=SymbolTable.symbolTable.get(tree.right.right.left.left.value+"$" + papa).name;
                             }
                             break;
                        default:
                                identifierType=tree.right.right.left.value;
                                break;
                        }
                        int myOrder=0;
                        if(!appearanceOrderForRecordSonPerStruct.isEmpty())
                        {
                            myOrder=appearanceOrderForRecordSonPerStruct.get(appearanceOrderForRecordSonPerStruct.size()-1);
                            appearanceOrderForRecordSonPerStruct.add(myOrder+1);//update the value
                        }
                        SymbolTable.symbolTable.put(tree.right.left.left.value+"$" + papa,new Variable(tree.right.left.left.value,"pointer",identifierType,innerMostStructFather,myOrder,pointerVarName,papa,null,depth));
                         
                        return;
                    }
                    if (tree.right.right!=null && tree.right.right.value.equalsIgnoreCase("record"))
                    {
                        int myOrder=0;
                        String previousFather="";
                        
                        if(!appearanceOrderForRecordSonPerStruct.isEmpty())
                        {
                            myOrder=appearanceOrderForRecordSonPerStruct.get(appearanceOrderForRecordSonPerStruct.size()-1);
                        }
                        appearanceOrderForRecordSonPerStruct.add(0);
                        if(!tree.right.left.left.value.equals(innerMostStructFather))
                            {
                                previousFather=innerMostStructFather; //saving previous father for me
                                innerMostStructFather=  tree.right.left.left.value;//update value if we have nested record
                            }
                        SymbolTable.generateSymbolTable(tree.right.right.left);
                        int recordAccumulatedSize=0;
                        int myAddress=5;
                        ArrayList<Variable> members= new ArrayList<Variable>();
                        for(Variable var : symbolTable.values())
                        {   //find vars defined under this struct and total their size
                            if (var.fatherRecord!=null){
                                if (var.fatherRecord.equals(tree.right.left.left.value))
                                {
                                    recordAccumulatedSize+=var.size;
                                    members.add(var);
                                    if(var.offset==0) myAddress=var.address;
                                }
                            }
                            if(!appearanceOrderForRecordSonPerStruct.isEmpty())appearanceOrderForRecordSonPerStruct.remove(appearanceOrderForRecordSonPerStruct.size()-1);
                        }
                        
                        //bubble sort members by order (offset)
                        for (int i=0;i<members.size();i++)
                        {
                        for (int j=i+1;j<members.size();j++)
                        {
                        	if (members.get(i).offset>members.get(j).offset)
                        	{
                        		Collections.swap(members,i,j);
                        	}
                        }
                     }
                        //maintain offsets inside record according to size
                        int sizeOfPreviousVar=0;
                        for (int i=0;i<members.size();i++)
                        {
                        	if (i!=0)
                        	{
                        		sizeOfPreviousVar=members.get(i-1).size;
                        		members.get(i).offset=sizeOfPreviousVar+members.get(i-1).offset;
                        	}
                        	else
                        	{
                        		members.get(i).offset=0;
                        	}
                        }
                        SymbolTable.symbolTable.put(tree.right.left.left.value+"$" + papa,new Variable(tree.right.left.left.value,"record",recordAccumulatedSize,previousFather,members,myOrder,myAddress,papa,null,depth));
                        innerMostStructFather=null; //done creating this record, so setting back to null
                        return;
                    }
                     
                    //read name and type, add to array list - regular variable in declarations list
                     if (tree.right.right!=null && tree.right.right.value.equalsIgnoreCase("array"))
                    {
                        int myOrder=0;
                        if(!appearanceOrderForRecordSonPerStruct.isEmpty())
                        {
                            myOrder=appearanceOrderForRecordSonPerStruct.get(appearanceOrderForRecordSonPerStruct.size()-1);
                            appearanceOrderForRecordSonPerStruct.add(myOrder+1);
                        }
                        //get range list (limits)
                        SymbolTable.generateSymbolTable(tree.right.right.left);
                        String identifierType;
                        String NameOfArray=null;
                        int cellSize=1;
                        switch (tree.right.right.right.value)//if array defined by another identifier need to figure out its type
                        {
                        case "identifier": //need to find the identifier type in symbol table and get its type
                            identifierType=SymbolTable.symbolTable.get(tree.right.right.right.left.value+"$" + papa).type;
                            cellSize=SymbolTable.symbolTable.get(tree.right.right.right.left.value+"$" + papa).size;
                            if(identifierType.equals("array")) NameOfArray=SymbolTable.symbolTable.get(tree.right.right.right.left.value+"$" + papa).name;
                             break;
                        default:
                                identifierType=tree.right.right.right.value;
                                cellSize=1;
                                break;
                        }
                        SymbolTable.symbolTable.put(tree.right.left.left.value+"$" + papa,new Variable(tree.right.left.left.value,"array",innerMostStructFather,arrayLimits,identifierType,cellSize,myOrder,NameOfArray,papa,null,depth));
                        //remove limits from static array
                        int i=symbolTable.get(tree.right.left.left.value+"$" + papa).dimensionsSizes.size();
                        i*=2; //for each dimension in the array- two entries in limits array
                        int j=arrayLimits.size()-1;
                        for (;i>0;i--,j--) //i- counter of how many limits to remove, j- index in the array where we need to remove it
                        {
                            arrayLimits.remove(j);
                        }
                        return;
                    }
                     if (tree.value.equals("array"))
                    {
                        //get range list (limits)
                        SymbolTable.generateSymbolTable(tree.left);
                        String identifierType;
                        String NameOfArray=null;
                        int cellSize=1;
                        if (tree.right.left!=null&& tree.right.left.equals("identifier"))//if array defined by another identifier need to figure out its type
                        {
                         //need to find the identifier type in symbol table and get its type
                            identifierType=SymbolTable.symbolTable.get(tree.right.left.left.value+"$" + papa).type;
                            cellSize=SymbolTable.symbolTable.get(tree.right.left.left.value+"$" + papa).size;
                            if(identifierType.equals("array")) NameOfArray=SymbolTable.symbolTable.get(tree.right.left.left.value+"$" + papa).name;
                        }
                        else {
                                identifierType=tree.right.value;
                                cellSize=1;
                        }
                        String reference=isByReferenceFlag==true? "byReference":"byValue";
                        SymbolTable.symbolTable.put(arrayInParamList+"$" + papa,new Variable(arrayInParamList,"array",innerMostStructFather,arrayLimits,identifierType,cellSize,0,NameOfArray,papa,reference,depth));
                        //remove limits from static array
                        int i=symbolTable.get(arrayInParamList+"$" + papa).dimensionsSizes.size();
                        i*=2; //for each dimension in the array- two entries in limits array
                        int j=arrayLimits.size()-1;
                        for (;i>0;i--,j--) //i- counter of how many limits to remove, j- index in the array where we need to remove it
                        {
                            arrayLimits.remove(j);
                        }
                        symbolTable.get(papaKey).paramList.add(symbolTable.get(arrayInParamList+"$" + papa));
                        return;
                    }
                     if(tree.value.equals("rangeList")){ //we will get here if we're in the inner most rangeList
                             
                         if (tree.left!=null)
                             generateSymbolTable(tree.left);
                             //add next lower limit
                            arrayLimits.add(Integer.parseInt(tree.right.left.left.value));
                            //add next high limit
                            arrayLimits.add(Integer.parseInt(tree.right.right.left.value));
                    }    
                    else
                    {
                         if (!tree.value.equals("declarationsList"))
                            return ;
                        int myOrder=0;
                        if(!appearanceOrderForRecordSonPerStruct.isEmpty())
                        {
                            myOrder=appearanceOrderForRecordSonPerStruct.get(appearanceOrderForRecordSonPerStruct.size()-1);
                            appearanceOrderForRecordSonPerStruct.add( myOrder+1);
                        }
                        String identifierType;
                        if(tree.right.right.left!=null)identifierType=SymbolTable.symbolTable.get(tree.right.right.left.left.value+"$" + papa).type;
                        else identifierType=tree.right.right.value;
                         
                        SymbolTable.symbolTable.put(tree.right.left.left.value+"$" + papa,new Variable(tree.right.left.left.value,identifierType,innerMostStructFather,myOrder,papa,null,depth));
                    }
                    return ;
                 }
        }
        
        private static void generatePCode(AST ast) 
        {
            if (ast!=null)
            {
                int tempLabel=label_counter;
                
                //calling function/procedure
                if(ast.value.equals("call")){
                	String calledFuncName=ast.left.left.value;
                	String calledFuncPapa="";
                	int calledFuncPapaDepth=0;
                	//calculating static link - search for papa of called func's depth and curr func's depth
                	for (Variable v : SymbolTable.symbolTable.values())//find papa
                	{
                		if(v.name.equals(calledFuncName))
                			calledFuncPapa=v.DefinedIn;
                	}
                	for (Variable v : SymbolTable.symbolTable.values())//find papa's depth
                	{
                		if(v.name.equals(calledFuncPapa))
                			calledFuncPapaDepth=v.absDepth;
                	}
                	System.out.println("mst "+(currDepth-calledFuncPapaDepth));
                	//go right for argument list - if there is such
                	if (ast.right!=null)
                		generatePCode(ast.right);
                	
                	//iterate through func's param list and calc sum of param sizes
                	int sumOfParams=0;
                	for (Variable v : SymbolTable.symbolTable.values())//find papa's depth
                	{
                		if(v.name.equals(calledFuncName))
                		{
                			for (Variable vb: v.paramList)
                			{
                				sumOfParams+=vb.size;
                			}
                			break;                				
                		}
                	}
                	//print cup
                	System.out.println("cup "+sumOfParams+" "+calledFuncName);
                	return;
                }
                
                //sending parameters to function/procedure
                if(ast.value.equals("argumentList")){
                	
                	if(ast.left!=null)//traverse left all the way down to push arguments by order
                		generatePCode(ast.left);
                	//handle current argument
                	switch(ast.right.value){
                		case "constInt":
                		case "constReal":
                		case "constBool":
                			System.out.println("ldc "+ast.right.left.value);
                			break;
                		case "identifier":
                			Variable tempVar=null;
                			for (Variable v : SymbolTable.symbolTable.values())//find papa's depth
                        	{
                				if(v.name.equals(ast.right.left.value)){
                					tempVar=v;
                					break;
                				}
                        	}
                			//handling params differently according to their type
            				switch(tempVar.type)
            				{
            				case "int":
            				case "real":
            				case "bool":
            					System.out.println("lda "+ (currDepth-tempVar.absDepth)+" "+ tempVar.address);
            					break;
            				case "array":
            				case "record":
            					System.out.println("lda "+ (currDepth-tempVar.absDepth)+ " "+ tempVar.address);
            					System.out.println("movs "+ tempVar.size);
            					break;
            				case "function":
            				case "procedure":
            					System.out.println("ldc "+tempVar.name);
            					break;
            				default:
            						break;
            				}
            				break;
            			 default:
            				 genPCodeForArguments(ast.right);
            				 break;
                		}
                	return;
                }
                
                //for while and if\else statements need to work in a different order due to labels
                if (ast.value.equals("while"))
                {
                    label_counter++;
                    innerMostWhileNum=tempLabel;
                    System.out.println("while_loop_"+tempLabel+":");
                    generatePCode(ast.left);//print condition code
                    //if our condition is a value of a variable- print ind
                    if ((ast.left!=null)&&(ast.left.value.equals("array")||ast.left.value.equals("record")||ast.left.value.equals("identifier")))
                	{
                		System.out.println("ind");
                	}
                    System.out.println("fjp end_while_loop_"+(tempLabel));
                    //label_counter++;
                    generatePCode(ast.right);
                    innerMostWhileNum=tempLabel;//reassigning in case there is a nested while loop
                    System.out.println("ujp while_loop_"+tempLabel);
                    System.out.println("end_while_loop_"+(tempLabel)+":");
                }
                //if without else case
                else if (ast.value.equals("if")&& (!ast.right.value.equals("else")))
                {
                    label_counter++;
                    generatePCode(ast.left);
                    if ((ast.left!=null)&&(ast.left.value.equals("array")||ast.left.value.equals("record")||ast.left.value.equals("identifier")))
                	{
                		System.out.println("ind");
                	}
                    System.out.println("fjp L"+tempLabel);
                    generatePCode(ast.right);
                    System.out.println("L"+tempLabel+":");
                }
                // if\else structure
                else if (ast.value.equals("if")&& (ast.right.value.equals("else")))
                {
                    label_counter++;
                    generatePCode(ast.left);
                    if ((ast.left!=null)&&(ast.left.value.equals("array")||ast.left.value.equals("record")||ast.left.value.equals("identifier")))
                	{
                		System.out.println("ind");
                	}
                    System.out.println("fjp L"+ tempLabel);
                    generatePCode(ast.right.left); //code to be done when if condition is true
                    int elseLable=(label_counter==(tempLabel+1)? tempLabel+1:label_counter+1);
                    label_counter++;
                    System.out.println("ujp L"+elseLable);
                    System.out.println("L"+tempLabel+":");
                    generatePCode(ast.right.right);//code to be done under else
                    System.out.println("L"+elseLable+":");
                }
                //switch-case
                else if (ast.value.equals("switch"))//check this condition in tree
                {
                    label_counter++;
                    generatePCode(ast.left);
                    if ((ast.left!=null)&&(ast.left.value.equals("array")||ast.left.value.equals("record")||ast.left.value.equals("identifier")))
                	{
                		System.out.println("ind");
                	}
                    innerMostSwitchNum=tempLabel;
                    System.out.println("neg");
                    System.out.println("ixj "+"end_switch_"+tempLabel);
                    generatePCode(ast.right);//handle caseList
                    //see how many cases - for each case print label case_#_switch label (saved in temp- to differentiate between switches across program
                    innerMostSwitchNum=tempLabel;
                    for(String s : caseList)
                    {
                        System.out.println("ujp " +s);
                        if (s.startsWith("case_1")) break;
                    }
                    System.out.println("end_switch_"+tempLabel+":");
                }
                //get cases for switch
                else if (ast.value.equals("caseList"))
                {
                    if (ast.left!=null) generatePCode(ast.left);
                    //print case label
                    String currCasenum=ast.right.left.left.value;
                    String printedCase="case_"+currCasenum+"_"+caseUniqueNum;
                    System.out.println("case_"+currCasenum+"_"+(caseUniqueNum--) +":");
                    caseList.add(0,printedCase);
                    generatePCode(ast.right.right);
                    System.out.println("ujp end_switch_"+innerMostSwitchNum);
                }                
                else if (ast.value.equals("record")&&indexFromRecord==false)
                {	
                    generatePCode(ast.left);
                    int offset=SymbolTable.symbolTable.get(ast.right.left.value+"$"+currFunc).offset;
                    if (SymbolTable.symbolTable.get(ast.right.left.value+"$"+currFunc).type.equals("pointer"))
                    	LatestPointedVar=SymbolTable.symbolTable.get(ast.right.left.value+"$"+currFunc).pointerToVariableName;
                    else LatestPointedVar=SymbolTable.symbolTable.get(ast.right.left.value+"$"+currFunc).name;
                    System.out.println("inc "+ offset);
                }
                
                else if (ast.value.equals("record")&&indexFromRecord==true)
                {	
                    generatePCode(ast.left);
                    int offset=SymbolTable.symbolTable.get(ast.right.left.value+"$"+currFunc).offset;
                    if (SymbolTable.symbolTable.get(ast.right.left.value+"$"+currFunc).type.equals("pointer"))
                    	LatestPointedVar=SymbolTable.symbolTable.get(ast.right.left.value+"$"+currFunc).pointerToVariableName;
                    else LatestPointedVar=SymbolTable.symbolTable.get(ast.right.left.value+"$"+currFunc).name;
                    String st=("inc "+ offset);
                    indexsFromRecordPrint.add(st);
                }
                
                else if (ast.value.equals("array" )&& indexFromArray==false)
                {
                	String myName="";
                    //Step 1- print lda with array address as calculated in symbol table generation
                	if (!ast.left.value.equals("identifier"))
                	{
                			generatePCode(ast.left);
                	}
                	else
                		{
                			int arrayInitalAddr=SymbolTable.symbolTable.get(ast.left.left.value+"$"+currFunc).address; 
                			int VarFatherDepth=0;
                			
                			if(SymbolTable.symbolTable.get(ast.left.left.value+"$"+currFunc).DefinedIn.equals(progName))
                				VarFatherDepth=0;//var is defined in prog - its father is not found in ST
                			else {
                				String fatherName=SymbolTable.symbolTable.get(ast.left.left.value+"$"+currFunc).DefinedIn;
                				for(Variable v : SymbolTable.symbolTable.values()){
                					if(v.name.equals(fatherName))
                						VarFatherDepth = v.absDepth; 
                				}
                			}
                			System.out.println("lda "+(currDepth-VarFatherDepth)+" "  +arrayInitalAddr);
                		    LatestPointedVar=ast.left.left.value;
                		}
                	if (!(ast.left.value).equals("identifier"))
                    {
                    	myName=LatestPointedVar;
                    }
                	else 
                		myName=SymbolTable.symbolTable.get(ast.left.left.value+"$"+currFunc).name;
                	
                	if (SymbolTable.symbolTable.get(myName+"$"+currFunc).ArrayName!=null &&ast.left.value.equals("array")&& ast.right.value.equals("indexList"))
                     {
                		 InnerArray=SymbolTable.symbolTable.get(myName+"$"+currFunc).ArrayName;
                     	//we are an array of array and need to handle its indexes
                     	generatePCode(ast.right);
                     }
                	 
                	 if (InnerArray.equals("")&&(!(ast.left.value).equals("identifier")))
                     {
                     	myName=LatestPointedVar;
                     }
                	 else if (InnerArray.equals("")) myName=SymbolTable.symbolTable.get(ast.left.left.value+"$"+currFunc).name;
                	 else myName=InnerArray;
                	 InnerArray="";
                    //step 2 - get list of indexes. the list will be added to end of array we need to iterate on the latest ones and then remove them!
                    generatePCode(ast.right);
                    //we know how many indexes we need to read - by num of dimensions
                   
                    int countOfIndexes=0;
                    countOfIndexes=SymbolTable.symbolTable.get(myName+"$"+currFunc).dimensionsSizes.size();
                    int startPosition=indexes.size()-countOfIndexes; //maybe +1 ? check
                    int j=1;//start position in dimensions
                    for (;startPosition<indexes.size();startPosition++,j++)//printing ldc +ixa for each index
                    {
                        if (indexes.get(startPosition).fromVariable==true)
                        {
                        	int depth=currDepth-indexes.get(startPosition).depth;
                            System.out.println("lda " +depth+ " "+indexes.get(startPosition).value);
                            System.out.println("ind");
                        }
                        else if (indexes.get(startPosition).fromOtherArray==true)
                        {
                            int numOfPrints=indexes.get(startPosition).value;
                            int i=indexsFromArrayPrint.size()-numOfPrints; //get the right place to start printing
                            while (i<indexsFromArrayPrint.size())
                            {
                                System.out.println(indexsFromArrayPrint.get(i));
                                i++;
                            }
                            System.out.println("ind");
                            while (numOfPrints>0)
                            {
                                i=indexsFromArrayPrint.size()-1; //need to remove these prints for next cases / next nest level
                                indexsFromArrayPrint.remove(i);
                                numOfPrints--;
                            }
                        }
                        else if (indexes.get(startPosition).fromRecord==true)
                        {
                             	int recordPrints=indexsFromRecordPrint.size();
                             	for (int i=0;i<recordPrints;i++)
                             	{
                             		System.out.println(indexsFromRecordPrint.get(i));
                             	}
                             	System.out.println("ind");
                             	 while (recordPrints>0)
                                 {
                                     int i=indexsFromRecordPrint.size()-1; //need to remove these prints for next cases / next nest level
                                     indexsFromRecordPrint.remove(i);
                                     recordPrints--;
                                 }
                             	indexFromRecord=false;
                        }
                        else
                        { //index is actual num
                            System.out.println("ldc " +indexes.get(startPosition).value);
                        }
                        int ixaValue=SymbolTable.symbolTable.get(myName+"$"+currFunc).cellSize; 
                        //iterating through array's dimensions
                        for (int i=j;i<SymbolTable.symbolTable.get(myName+"$"+currFunc).dimensionsSizes.size();i++)
                        {
                            ixaValue*=SymbolTable.symbolTable.get(myName+"$"+currFunc).dimensionsSizes.get(i);
                        }
                        System.out.println("ixa " +ixaValue);
                    }
                    //after getting all indexes - dec subpart
                    System.out.println("dec " +SymbolTable.symbolTable.get(myName+"$"+currFunc).subpart);
                    //clean up used indexes from array
                    startPosition=indexes.size()-countOfIndexes;
                    for (int i=indexes.size()-1;i>=startPosition;i--)
                    {
                        indexes.remove(i);
                    }
                }
                else if (ast.value.equals("array" )&& indexFromArray==true) //handling the same as above but saving prints to list
                {
                    //Step 1- print ldc with array address as calculated in symbol table generation
                    int countOfPrints=0;
                    String myName="";
                   // int arrayInitalAddr=SymbolTable.symbolTable.get(ast.left.left.value).address;
                    String str=""; //"ldc " +arrayInitalAddr;
                    
                    if (!ast.left.value.equals("identifier"))
                	{
                			generatePCode(ast.left);
                	}
                	else
                		{
                			int arrayInitalAddr=SymbolTable.symbolTable.get(ast.left.left.value+"$"+currFunc).address;
                			int VarFatherDepth= SymbolTable.symbolTable.get(SymbolTable.symbolTable.get(ast.left.left.value+"$"+currFunc).DefinedIn+"$"+currFunc).absDepth; 
                		    
                			str="lda "+(currDepth-VarFatherDepth)+" "  +arrayInitalAddr;
                			indexsFromArrayPrint.add(str); countOfPrints++;
                			LatestPointedVar=ast.left.left.value;
                		}
                    if (!(ast.left.value).equals("identifier"))
                    {
                    	myName=LatestPointedVar;
                    }
                	else 
                		myName=SymbolTable.symbolTable.get(ast.left.left.value+"$"+currFunc).name;
                	
                	if (SymbolTable.symbolTable.get(myName+"$"+currFunc).ArrayName!=null &&ast.left.value.equals("array")&& ast.right.value.equals("indexList"))
                     {
                		 InnerArray=SymbolTable.symbolTable.get(myName+"$"+currFunc).ArrayName;
                     	//we are an array of array and need to handle its indexes
                     	generatePCode(ast.right);
                     }
                	 
                	 if (InnerArray.equals("")&&(!(ast.left.value).equals("identifier")))
                     {
                     	myName=LatestPointedVar;
                     }
                	 else if (InnerArray.equals(""))myName=SymbolTable.symbolTable.get(ast.left.left.value+"$"+currFunc).name;
                	 else myName=InnerArray;
                	 InnerArray="";
                    
                    //step 2 - get list of indexes. the list will be added to end of array we need to iterate on the latest ones and then remove them!
                    generatePCode(ast.right);
                    //we know how many indexes we need to read - by num of dimensions
                    int countOfIndexes=0;
                    countOfIndexes=SymbolTable.symbolTable.get(myName+"$"+currFunc).dimensionsSizes.size();
                    int startPosition=indexes.size()-countOfIndexes; //maybe +1 ? check
                    int j=1;//start position in dimensions
                    for (;startPosition<indexes.size();startPosition++,j++)//printing ldc +ixa for each index
                    {
                        if (indexes.get(startPosition).fromVariable==true)
                        {
                            str="ind";
                            indexsFromArrayPrint.add(str);countOfPrints++;
                        }
                        else if (indexes.get(startPosition).fromOtherArray==true)
                        {
                            int numOfPrints=indexes.get(startPosition).value;
                            int i=indexsFromArrayPrint.size()-numOfPrints; //get the right place to start printing
                            while (i<indexsFromArrayPrint.size())
                            {
                                str=(indexsFromArrayPrint.get(i));countOfPrints++;
                                indexsFromArrayPrint.add(str);
                                i++;
                            }
                            str=("ind");countOfPrints++;
                            indexsFromArrayPrint.add(str);
                           
                        }
                        else if (indexes.get(startPosition).fromRecord==true)
                        {
                        	int recordPrints=indexsFromRecordPrint.size();
                        	for (int i=0;i<recordPrints;i++)
                        	{
                        		str=(indexsFromArrayPrint.get(i));countOfPrints++;
                        		indexsFromArrayPrint.add(str);
                        	}
                        	 str=("ind");countOfPrints++;
                        	 indexsFromArrayPrint.add(str);
                        	 indexFromRecord=false;
                        }
                        else
                        {
                        	str="ldc " +indexes.get(startPosition).value;
                            indexsFromArrayPrint.add(str);countOfPrints++;
                        }
                        
                        int ixaValue=SymbolTable.symbolTable.get(myName+"$"+currFunc).cellSize; 
                        //iterating through array's dimensions
                        for (int i=j;i<SymbolTable.symbolTable.get(myName+"$"+currFunc).dimensionsSizes.size();i++)
                        {
                            ixaValue*=SymbolTable.symbolTable.get(myName+"$"+currFunc).dimensionsSizes.get(i);
                        }
                        str="ixa " +ixaValue;
                        indexsFromArrayPrint.add(str);countOfPrints++;
                       // j++;
                    }
                    //after getting all indexes - dec subpart
                    str="dec " +SymbolTable.symbolTable.get(myName+"$"+currFunc).subpart;
                    indexsFromArrayPrint.add(str);countOfPrints++;
                    //clean up used indexes from array
                    startPosition=indexes.size()-countOfIndexes;
                    for (int i=indexes.size()-1;i>=startPosition;i--)
                    {
                        indexes.remove(i);
                    }
                    indexes.get(indexes.size()-1).value=countOfPrints;
                    
                    indexFromArray=false;
                }
                else if (ast.value.equals("indexList"))
                {
                    if (ast.left!= null) generatePCode(ast.left);
                    //differentiate between cases of index that is identifier or index that is a number
                    if(ast.right.value.equals("identifier")) //in this case we need to get the value of the variable from symbol table
                    {
                        Integer value=SymbolTable.symbolTable.get(ast.right.left.value+"$"+currFunc).address;
                        int depth=SymbolTable.symbolTable.get(ast.right.left.value+"$"+currFunc).absDepth;
                        indexes.add(new Index(value,true,false,false,depth));
                    }
                    else if (ast.right.value.equals("array")) //getting index from a value stored in an array
                    {
                        indexFromArray=true;
                        indexes.add(new Index(0,false,true,false,-1));//adding index by order, marking it as from array
                        generatePCode(ast.right); /*-1 is here to indicate that investigation of situation needs to occur*/
                    }
                    else if (ast.right.value.equals("record")) //getting index from a value stored in an array
                    {
                        indexFromRecord=true;
                        indexes.add(new Index(0,false,false,true,-1));//adding index by order, marking it as from record
                        generatePCode(ast.right);/*-1 is here to indicate that investigation of situation needs to occur*/
                    }
                    else
                        {
                            Integer value=Integer.parseInt(ast.right.left.value);
                            indexes.add(new Index(value,false,false,false,0));
                        }
                }
                //all other command should follow post order traversal
                else
                {
                    generatePCode(ast.left);
                    //figure out if we need value and need to print ind
                    switch (ast.value)
                    {
                    case "print":
                    case "multiply":
                    case "divide":
                    case "minus":
                    case "plus":
                    case "not":
                    case "or":
                    case "and":
                    case "lessThan":
                    case "lessOrEquals":
                    case "negative":
                    case "greaterThan":
                    case "equals":
                    case "notEquals":
                    case "greaterOrEquals":
                    	if ((ast.left!=null)&&(ast.left.value.equals("array")||ast.left.value.equals("record")||ast.left.value.equals("identifier")))
                    	{
                    		System.out.println("ind");
                    	}
                    default:
                    	break;
                    }
                    generatePCode(ast.right);
                    switch (ast.value)
                    {
                    case "print":
                    case "assignment":
                    case "multiply":
                    case "divide":
                    case "minus":
                    case "plus":
                    case "or":
                    case "and":
                    case "not":
                    case "lessThan":
                    case "lessOrEquals":
                    case "negative":
                    case "greaterThan":
                    case "equals":
                    case "notEquals":
                    case "greaterOrEquals":
                    	if ((ast.right!=null)&&(ast.right.value.equals("array")||ast.right.value.equals("record")||ast.right.value.equals("identifier")))
                    	{
                    		System.out.println("ind");
                    	}
                    default:
                    	break;
                    }
                    
                    switch (ast.value)
                    {
                    case "plus":
                        System.out.println("add");
                        return;
                    case "multiply":
                        System.out.println("mul");
                        return;
                    case "minus": 
                        System.out.println("sub");
                        return;
                    case "divide":
                        System.out.println("div");
                        return;
                    case "identifier":
                    	String s="";
                    	if(ast.left.value.equals(currFunc)){
                    		s="lda 0 0";
                    	}
                    	else{
                    		int address=SymbolTable.symbolTable.get(ast.left.value+"$"+currFunc).address;
                    		LatestPointedVar=ast.left.value;
                    		s=("lda "+(currDepth-SymbolTable.symbolTable.get(ast.left.value+"$"+currFunc).absDepth) + " "+address);
                    	}
                        if(indexFromRecord==true)
                        {
                        	  indexsFromRecordPrint.add(s);
                        }
                        else System.out.println(s);
                        return;
                    case "constInt":
                    case "constReal":
                    case "constBool":
                        System.out.println("ldc "+ast.left.value);
                        return;
                    case "print":
                        System.out.println("print");
                        return;
                    case "assignment":
                        System.out.println("sto");
                        return;
                    case "false": 
                        System.out.println("ldc 0");
                        return;
                    case "true":
                        System.out.println("ldc 1");
                        return;
                    case "not":
                        System.out.println("not");
                        return;
                    case "negative":
                        System.out.println("neg");
                        return;
                    case "or":
                        System.out.println("or");
                        return;
                    case "and":
                        System.out.println("and");
                        return;
                    case "lessOrEquals":
                        System.out.println("leq");
                        return;
                    case "lessThan":
                        System.out.println("les");
                        return;
                    case "greaterThan":
                        System.out.println("grt");
                        return;
                    case "greaterOrEquals":
                        System.out.println("geq");
                        return;
                    case "notEquals":
                        System.out.println("neq");
                        return;
                    case "equals":
                        System.out.println("equ");
                        return;
                    case "pointer":
                        System.out.println("ind");
                        if(SymbolTable.symbolTable.get(LatestPointedVar+"$"+currFunc).pointerToVariableName != null)
                        	LatestPointedVar=SymbolTable.symbolTable.get(LatestPointedVar+"$"+currFunc).pointerToVariableName;
                        return;
                    case "break":
                        System.out.println("ujp end_while_loop_"+(innerMostWhileNum));//check with nested while loops and nested ifs 
                        return;
                    default:
                            return;
                            
                    }
                }
            }
            return;
        }
        public static void genPCodeForArguments(AST tree)
        {
        	if(tree == null)
        		return;
        	genPCodeForArguments(tree.left);
        	genPCodeForArguments(tree.right);
        	switch(tree.value){
        		case "plus":
        			System.out.println("add");
        			return;
        		case "multiply":
        			System.out.println("mul");
        			return;
        		case "minus": 
        			System.out.println("sub");
        			return;
        		case "divide":
        			System.out.println("div");
        			return;
        		case "identifier":
       
        			if(tree.left.value.equals(currFunc)){
        				System.out.println("lda 0 0");
        			}
        			else{
        				int address=SymbolTable.symbolTable.get(tree.left.value+"$"+currFunc).address;
        				System.out.println("lda "+(currDepth-SymbolTable.symbolTable.get(tree.left.value+"$"+currFunc).absDepth) + " "+address);
        			}
        			System.out.println("ind");
        			return;
        		case "constInt":
        		case "constReal":
        		case "constBool":
        			System.out.println("ldc "+tree.left.value);
        			return;
           
        		case "false": 
        			System.out.println("ldc 0");
        			return;
        		case "true":
        			System.out.println("ldc 1");
        			return;
        		case "not":
        			System.out.println("not");
        			return;
        		case "negative":
        			System.out.println("neg");
        			return;
        		case "or":
        			System.out.println("or");
        			return;
        		case "and":
        			System.out.println("and");
        			return;
        		case "lessOrEquals":
        			System.out.println("leq");
        			return;
        		case "lessThan":
        			System.out.println("les");
        			return;
        		case "greaterThan":
        			System.out.println("grt");
        			return;
        		case "greaterOrEquals":
        			System.out.println("geq");
        			return;
        		case "notEquals":
        			System.out.println("neq");
        			return;
        		case "equals":
        			System.out.println("equ");
        			return;
        		case "pointer":
        			System.out.println("ind");
        			if(SymbolTable.symbolTable.get(LatestPointedVar+"$"+currFunc).pointerToVariableName != null)
        				LatestPointedVar=SymbolTable.symbolTable.get(LatestPointedVar+"$"+currFunc).pointerToVariableName;
        			return;
            
            default:
                    return;
                    
        	}
        }
        
        
        
        
        public static boolean isShoe(Object o)
        {
        	return (o==null);
        }
        public static void generatePCodeForFunctions(AST tree){
        	
        	if(isShoe(tree))
        		return;
        	if(tree.left!=null && tree.left.value.equals("functionsList"))
        		generatePCodeForFunctions(tree.left); //traverse left all the way down to get to 1st func\proc code
        	if(tree.right != null &&(tree.right.value.equals("function") || (tree.right.value.equals("procedure")))){
        		String Name=tree.right.left.left.left.value;
        		
        		System.out.println(Name +":");
        		int LocalVars=5;
                for(Variable v : SymbolTable.symbolTable.values()){
                	if(v.DefinedIn.equals(Name))
                		LocalVars+=v.size;
                }
                
                System.out.println("ssp " + LocalVars );
                System.out.println("ujp "+Name+"_begin");
                if (tree.right.right.left.right!=null && tree.right.right.left.right.value.equals("functionsList")
                    {
                        //this means there is another func\proc defined in the scope of current function - need to print out its code
                        generatePCodeForFunctions(tree.right.right.left.right);
                    }
                System.out.println(Name+"_begin:");
                String type=tree.right.value.substring(0, 1);//first letter of function/procedure
                currDepth=SymbolTable.symbolTable.get(Name+"$"+currFunc).absDepth;
                currFunc=Name;
                generatePCode(tree.right.right.right);
        		System.out.println("ret"+ type);
        	}
        	return;       	
        }
        
        
        public static String progName;
        public static String currFunc;
        public static boolean indexFromArray=false;
        public static boolean indexFromRecord=false;
        public static boolean LeftOfAssignment=false;
        public static String LatestPointedVar="";
        public static String InnerArray="";
        public static ArrayList<String> caseList= new ArrayList<String>();
        public static ArrayList<String> indexsFromArrayPrint= new ArrayList<String>();
        public static ArrayList<String> indexsFromRecordPrint= new ArrayList<String>();
        public static ArrayList<Index> indexes= new ArrayList<Index>();
        public static int innerMostSwitchNum=-1;
        public static int caseUniqueNum=100;
        public static int innerMostWhileNum=-1;
        public static int label_counter=0;
        public static int currDepth=0;
        public static boolean isAssignmet=false; //helps to differentiate between cases when an identifier's address is needed and when its value is needed (true for address only)
        public static void main(String[] args) throws FileNotFoundException 
        {
        	File file =new File ("C:\\Users\\Valerie\\Desktop\\JavaProjects\\hw3Compilers\\Samples3\\tree14.txt");
            Scanner scanner = new Scanner(file);
            AST ast = AST.createAST(scanner);
            progName=ast.left.left.left.value;
            SymbolTable.papa=progName;
            
            if (ast.right.left!=null)
            	SymbolTable.generateSymbolTable(ast.right.left);//starting from scope
            
            
            System.out.println(progName + ":");
            int sizeOfLocalVarsOfProg=5;
            for(Variable v : SymbolTable.symbolTable.values()){
            	if(v.DefinedIn.equals(progName)&&v.fatherRecord==null)//variables inside record should not be counted- their size is reflected by their father record size
            		sizeOfLocalVarsOfProg+=v.size;
            }
            System.out.println("ssp " + sizeOfLocalVarsOfProg );
            System.out.println("ujp " + progName + "_begin");
            //print functions code if exists
            currFunc=progName;
            if (ast.right.left.right!=null && ast.right.left.right.value.equals("functionsList"))
            	generatePCodeForFunctions(ast.right.left.right);//send first node with 'functionsList', if exists
            
            System.out.println(progName + "_begin:");
            currDepth=0;
            currFunc=progName;
            generatePCode(ast.right.right);
            System.out.println("stp");
            scanner.close();
        }
         
    }