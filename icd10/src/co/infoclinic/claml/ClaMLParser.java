package co.infoclinic.claml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import org.dom4j.io.SAXReader;

public class ClaMLParser {
	
	static boolean isModifier = false;
	static boolean isModifierClass = false;
	static boolean isClass = false;
	static boolean isMeta = false;
	static boolean isSubClass = false;
	static boolean isSuperClass = false;
	static boolean isModifiedBy=false;
	static boolean isModifiedByForRubric = false;
	static boolean isExcludeModifier = false;
	
	static boolean isRubric = false;
	static boolean isPreferred = false;
	
	static boolean isLabel = false;
	static boolean isPara = false;
	static boolean isFragment = false;
	static boolean isList = false;
	static boolean isTable = false;
	
	static boolean isTerm = false;
	static boolean isRef = false;
	static boolean isLabelRef = false;
	
	static boolean isLeafElement=false;

	static String modifier = "";
	static String codeForModifier = "";

	static ArrayList<String> modifiedBy = new ArrayList<String>() ;
	static ArrayList<String> excludeModifier = new ArrayList<String>();
	static ArrayList<String> subClass = new ArrayList<String>();
	
	static String code = "";
	static String version = "2019";
	static String id="";
	static String classKind = "";
	static String classUsageKind = "";
	static String modifierClassUsageKind = "";
	static String superClass = "";
	static String label = "";
	
	static String kind = "";
	static String lang="";
	static String paraClass = "";
	static String fragmentType = "";
	static String ref = "";
	static String labelRef = "";
		
	static int tabCount=1;
	static String attrChars = ""; // for attribute
	static LinkedHashMap<String, LinkedHashMap<String, String>> meta = new LinkedHashMap<String, LinkedHashMap<String, String>>();

	static ArrayList<ICD10Modifier> icd10Modifiers = new ArrayList<ICD10Modifier>();
	static ArrayList<ICD10ModifierClass> icd10ModifierClasses = new ArrayList<ICD10ModifierClass>();
	static ArrayList<ICD10Class> icd10Classes = new ArrayList<ICD10Class>();
	static ArrayList<ICD10Rubric> icd10Rubrics = new ArrayList<ICD10Rubric>();
	
    /*
    <ClassKinds>
            <ClassKind name="category"/>
            <ClassKind name="block"/>
            <ClassKind name="chapter"/>
    </ClassKinds>
    <UsageKinds>
            <UsageKind mark="*" name="aster"/>
            <UsageKind mark="+" name="dagger"/>
    </UsageKinds>
    <RubricKinds>
            <RubricKind inherited="false" name="footnote"/>
            <RubricKind inherited="false" name="text"/>
            <RubricKind inherited="false" name="coding-hint"/>
            <RubricKind inherited="false" name="definition"/>
            <RubricKind inherited="false" name="introduction"/>
            <RubricKind inherited="false" name="modifierlink"/>
            <RubricKind inherited="false" name="note"/>
            <RubricKind inherited="false" name="exclusion"/>
            <RubricKind inherited="false" name="inclusion"/>
            <RubricKind inherited="false" name="preferredLong"/>
            <RubricKind inherited="false" name="preferred"/>
    </RubricKinds>

	*/
	
	
	public static void main(String[] args) throws IOException {
		//if ("category".equals(classKind.category)) ;
		
		File inputFile;	
		File classFile;
		File rubricFile;
		File xmlFile;
		
		//if no parameters are give, use default location
		if (args.length >= 2) {
			inputFile = new File(args[0]);
			classFile = new File("ICD10Class.txt");
			rubricFile = new File("ICD10Rubric.txt");
		} else {
			inputFile = new File("/Users/seungjong.yu/github/term/icd10/bin/icd102019en.xml");
			classFile = new File("ICD10Class.txt");
			rubricFile = new File("ICD10Rubric.txt");
		}

		BufferedWriter out1 = new BufferedWriter(new FileWriter(classFile));
		BufferedWriter out2 = new BufferedWriter(new FileWriter(rubricFile));
		//BufferedWriter out3 = new BufferedWriter(new FileWriter(xmlFile));
		
		System.out.println(System.getProperty("user.dir"));
		
		System.out.println("ClaMLParser: Running....");
		
		System.out.println("Reading XML from " + inputFile.toString());

		
		try {
	        SAXReader reader = new SAXReader();
	        Document idocument = reader.read( inputFile );
	        //Document odocument = DocumentHelper.createDocument();
	        
	        // Hierarchy Root setting
	        ICD10Class c1 = new ICD10Class();
	        c1.setCode("ICD10 2019");
			c1.setVersion("2019");
			c1.setClassKind("");
			c1.setLabel("International Statistical Classification of Diseases and Related Health Problems 10th Revision");			
			c1.setChildrenCount(0);
			c1.setDescendantCount(0);
			c1.setPath("ROOT");	
			icd10Classes.add(c1);
	         
	         Element rootElement = idocument.getRootElement();
	         
	         /*
	          * Meta data setting
	          */

	         //ClaMLRoot clamlRoot = new ClaMLRoot(rootElement);
	         
	         /*
	          * <Class>
	          */
	 					
	         for (Iterator<Element> elements = rootElement.elementIterator(); elements.hasNext();) {
				Element e = elements.next();
				
				parseElement(e);
				
	        }
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		try {
			writeFile(classFile, rubricFile, true);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ClaMLParser: Error writing Class");
		}

		System.out.println("ClaMLParser: Done");
		
	}
	
	public static void parseElement(Element element) throws IOException {
		
		ICD10Modifier icd10Modifier = new ICD10Modifier();
		ICD10ModifierClass icd10ModifierClass = new ICD10ModifierClass();
		ICD10Class icd10Class = new ICD10Class();
		ICD10Rubric icd10Rubric = new ICD10Rubric();
		
		if (element.getQualifiedName().equals("Modifier")) {
			codeForModifier = element.attributeValue("code");
			isModifier = true;
			subClass.clear();
		} else if (element.getQualifiedName().equals("ModifierClass")) {
			code = element.attributeValue("code");
			modifier = element.attributeValue("modifier");
			classKind = "category";
			if ( element.attributeCount() == 3 ) {
				modifierClassUsageKind = element.attributeValue("usage");
			}
			isModifier = false;
			isModifierClass = true;
		} else if (element.getQualifiedName().equals("Class")) {
			if (icd10Rubrics.size() != 0) {
				ICD10Rubric r = icd10Rubrics.get(icd10Rubrics.size()-1);
				if ( r.getEndRubric().equals("m")) {
					r.endRubric = "end";
					icd10Rubrics.remove(icd10Rubrics.size()-1);
					icd10Rubrics.add(r);
					//System.out.println(r.getCode() + " " + r.getKind() + " " + r.getLabel() + " " + r.getEndRubric());
				} else if (r.getEndRubric().equals("m2")) {
					r.endRubric = "end2";
					icd10Rubrics.remove(icd10Rubrics.size()-1);
					icd10Rubrics.add(r);
				}
			}
			code = element.attributeValue("code");
			classKind = element.attributeValue("kind");
			if ( element.attributeCount() == 3 ) {
				classUsageKind = element.attributeValue("usage");
			}
			
			superClass= new String();
			
			isModifierClass = false;
			isClass = true;
			
			//// modifierBy = ""; incompletely reset
			modifiedBy = new ArrayList<String>();
			excludeModifier = new ArrayList<String>();
			
		} 
		
		Element e = null;
		Iterator<Element> elements = element.elementIterator();		// down to child element
		if (elements.hasNext()) {
					
			for (; elements.hasNext();) {
				e = elements.next(); 
				
				isMeta=false;
				isSuperClass=false;
				isSubClass=false;
				
													
				if (e.getQualifiedName().equals("Meta")) {
					LinkedHashMap<String, String> tmpMap = new LinkedHashMap<String, String>();
					tmpMap.put(e.attributeValue("code"), e.attributeValue("value"));
					meta.put(code, tmpMap);
					isMeta = true;
				} else if (e.getQualifiedName().equals("SubClass")) {
					subClass.add(e.attributeValue("code"));
					isSubClass = true;
				} else if (e.getQualifiedName().equals("SuperClass")) {
					superClass = e.attributeValue("code");					
					isSuperClass = true;
				} else if (e.getQualifiedName().equals("ModifiedBy")) {
					int i = modifiedBy.size();
					boolean dup = false;
					for (int l = 0; l < i; l++) {
					    if (e.attributeValue("code").contains(modifiedBy.get(l))) {
					    	dup = true;
					    } 
					}
					if (modifiedBy.size() == 0 || !dup) {
						modifiedBy.add(e.attributeValue("code"));
					}
					isModifiedBy = true;
					//System.out.println(modifiedBy);
				} else if (e.getQualifiedName().equals("ExcludeModifier")) {
					excludeModifier.add(e.attributeValue("code"));
					isExcludeModifier = true;
					//System.out.println(excludeModifier);
				}
				
				if (e.getQualifiedName().equals("Rubric")) {
					id = e.attributeValue("id");
					kind = e.attributeValue("kind");
				} else if (e.getQualifiedName().equals("Label")) {
					lang = e.attributeValue("lang");
					if (kind.equals("preferred") || kind.equals("preferredLong") || kind.equals("inclusion") || kind.equals("exclusion") || kind.equals("footnote")) {
						
						if (kind.equals("preferred")) {
							isPreferred = true;
						}
						
						if (e.asXML().contains("Fragment")) {
							label = parseFragment(e);
							isLeafElement = true;
						} else if (e.asXML().contains("Term")) {
							label = parseTerm(e);
							isLeafElement = true;
						} else if (e.asXML().contains("Reference")){
							label = e.getText();
							isLabelRef = true;
							parseRef(e);
							isLeafElement = true;
						} else {
							label = e.getText();
							isLeafElement = true;
						}
						isLabel = true;
						//System.out.println(code + " " + kind + " " + label);
					} else if (kind.equals("text")) {
						if (!e.asXML().contains("Para") && !e.asXML().contains("Fragment") && !e.asXML().contains("List") && !e.asXML().contains("Table")) {
							label = e.getText();
							isLabel = true;
							isLeafElement = true;
						}
					} else if (kind.equals("coding-hint")) {
						if (e.asXML().contains("Reference")) {
							label = e.getText();
							parseRef(e);
							isLabel = true;
							isLeafElement = true;
						} else if (!e.asXML().contains("Para") && !e.asXML().contains("Fragment") && !e.asXML().contains("List") && !e.asXML().contains("Table")) {
							label = e.getText();
							isLabel = true;
							isLeafElement = true;
						}
							
					} else if (kind.equals("definition")) {
						if (e.asXML().contains("Para")) {
							label = parsePara(e);
							isLabel = true;
							isLeafElement = true;
						} else {
							label = e.getText();
							label = label.replaceAll("\n","");
							label = label.replaceAll("\t", " ");
							label = label.replaceAll("\\p{javaSpaceChar}{2,}"," ");
							isLabel = true;
							isLeafElement = true;
						}
					}
				} else if (e.getQualifiedName().equals("Para")) {
					label = parsePara(e);
					isLeafElement = true;
				} else if (e.getQualifiedName().equals("List")) {				
					label = parseList(e);
					isLeafElement = true;
				} else if (e.getQualifiedName().equals("Table") ) {
					label = parseTable(e);
					isLeafElement = true;
					//System.out.println("Table : " + label);
				}
				
				//System.out.println(e.getQualifiedName() + " " + kind + " " + label);

				/*
				 * skip parseElement if isXXX flag is set to true 
				 * isXXX mean current Element is leaf element so no element needs to process 
				 * for example. fragment, list and reference
				 */
				
				if (!isLabel && !isPara && !isFragment && !isList && !isTable && !isTerm && !isRef) {

					parseElement(e);
				}
				
				//System.out.println("===" + e.getQualifiedName() + " " + kind + " " + label);
					
				if (isLabel) { 
					isLabel=false;
				} else if (isList) {
					isList=false;
				} else if (isTable) {
					isTable=false;
				} else if (isPara) {
					isPara=false;
				} else if (isFragment) {
					isFragment=false;
				} if (isTerm) {
					isTerm=false;
				} else if (isRef) {
					isRef=false;
				} else if (isLabel) {
					isLabel=false;
				} else if (isPara) {
					isPara=false;
				} 
				
				//System.out.println(isPreferred + " " + isClass + " " + ref);
				if ( !isMeta && !isSubClass && !isSuperClass) {
					isMeta=false;
					isSubClass=false;
					isSuperClass=false;
					
					if (isLeafElement && isModifier) {
						/*
						System.out.println(codeForModifier + " "
								+ code + " "
								+ subClass + " "
								+ version + " "
								+ id + " "
								+ kind + " "
								+ "" + " "
								+ lang + " "
								+ fragmentType + " "
								+ "" + " "
								+ label + " "
								+ ref
								);
						*/
						//icd10Modifier.setModifiedBy(codeForModifier);
						//codeForModifier = "";
						
						icd10Modifier.setCode(codeForModifier);
						icd10Modifier.setSubClass(subClass);
						icd10Modifier.setVersion(version);
						icd10Modifier.setId(id);
						icd10Modifier.setKind(kind);
						icd10Modifier.setUsageKind(""); //usageKind);
						icd10Modifier.setLang(lang);
						icd10Modifier.setFragmentType(fragmentType);
						fragmentType="";
						icd10Modifier.setParaClass("");
						icd10Modifier.setLabel(label);
						icd10Modifier.setRef(ref);
						icd10Modifiers.add(icd10Modifier);
						icd10Modifier = new ICD10Modifier();
						
						
						ref="";
						isRef = false;
						
						isLeafElement = false;
						
					} else if (isLeafElement && isModifierClass) {
						/*
						System.out.println(
								modifier + " "
								+ code + " "
								+ version + " "
								+ classKind + " "
								+ kind + " "
								+ modifierClassUsageKind + " "
								+ superClass + " "
								+ label + " "
								+ ref
								);
						*/
						////System.out.println(modifier + " " + code + " " + kind + " " + label);
						
						icd10ModifierClass.setModifier(modifier);
						
						icd10ModifierClass.setCode(code);
						icd10ModifierClass.setVersion(version);
						icd10ModifierClass.setId(id);
						icd10ModifierClass.setClassKind(classKind);
						icd10ModifierClass.setKind(kind);
						icd10ModifierClass.setUsageKind(modifierClassUsageKind);
						modifierClassUsageKind="";
						icd10ModifierClass.setLang(lang);
						icd10ModifierClass.setFragmentType(fragmentType);
						fragmentType="";
						icd10ModifierClass.setParaClass(paraClass);
						paraClass="";
						icd10ModifierClass.setSuperClass(superClass);
						icd10ModifierClass.setLabel(label);
						icd10Class.setRef(ref);
						
						ref="";
						isRef=false;
						//icd10ModifierClass.setChildCount(childrenCount);
						//icd10ModifierClass.setDescendantCount(descendantCount);
						//icd10ModifierClass.setPath(path);	
						icd10ModifierClasses.add(icd10ModifierClass);
						icd10ModifierClass = new ICD10ModifierClass();
													
						isLeafElement = false;
						
					} else if (isLeafElement && isClass) {
						
						if (isPreferred) {
							//System.out.println(modifiedBy + " " + icd10Class.getModifiedBy());
							
							if (!modifiedBy.isEmpty()) {
								icd10Class.setModifiedBy(modifiedBy);
								//System.out.println(code + " " + superClass + " " + icd10Class.getModifiedBy() );
								
							} else {
								//ArrayList<ICD10Class> tmp = icd10Classes;
								for (ICD10Class c : icd10Classes) {
									//System.out.println(c.code + " " + c.superClass + " " + c.getModifiedBy());
									if (!superClass.isEmpty() && c.getCode().equals(superClass)) {
										icd10Class.setModifiedBy(c.getModifiedBy());
										//System.out.println(code + " " + superClass + " " + c.getModifiedBy() );
	
									}
								}
							}
							
							if (!excludeModifier.isEmpty()) {
								icd10Class.setExcludeModifier(excludeModifier);
							} /*else {
								for (ICD10Class ex : icd10Classes) {
									if (!superClass.isEmpty() && ex.getCode().equals(superClass)) {
										icd10Class.setExcludeModifier(ex.getExcludeModifier());
									}
								}
							}*/
							
							icd10Class.setCode(code);
							icd10Class.setVersion(version);
							icd10Class.setClassKind(classKind);
							icd10Class.setUsageKind(classUsageKind);
							if ("chapter".equals(classKind)) {
								superClass = "ICD10 2019";
								icd10Class.setSuperClass(superClass);
							} else {
								icd10Class.setSuperClass(superClass);
							}
							icd10Class.setLabel(label);
							icd10Class.setRef(labelRef);
							//icd10Class.setChildCount(childrenCount);
							//icd10Class.setDescendantCount(descendantCount);
							//icd10Class.setPath(path);	
							
							/* 
							 * ChildrenCount & Path
							 */
							for (int i=0; i < icd10Classes.size(); i++ ) {
								ICD10Class c2 = icd10Classes.get(i);
								//System.out.println("Classes code:"+c2.getCode() + " " + " " + c2.getChildrenCount());
								if (superClass.equals(c2.getCode())) {
									c2.setChildrenCount(c2.getChildrenCount()+1);
									if ("ROOT".equals(c2.getPath()) ) {
										icd10Class.setPath(c2.getCode());
									} else {
										icd10Class.setPath(c2.getPath() + "~" + superClass);
									}
									
									icd10Classes.set(i, c2);
									//System.out.println("Class code:"+code + " " + c2.getCode() + " " + c2.getChildrenCount() + "\n");
								}
							}
							
							icd10Classes.add(icd10Class);
							icd10Class = new ICD10Class();
							
							labelRef="";
							isLabelRef=false;
							isPreferred = false;
							//isClass = false;
							
						}
						
						/*
						 * fill ModifiedBy(for example. J96_5) to descendant
						 * 
						 * note : 	fill "m" in original Class and fill "end" in original Class' last member
						 * 			for write icd10Modifier after icd10Rubric  
						 */
						if (!modifiedBy.isEmpty()) {
							icd10Rubric.setModifiedBy(modifiedBy);
							icd10Rubric.setEndRubric("m");
						} else {
							for (ICD10Rubric r : icd10Rubrics) {
								if ( !superClass.isEmpty() && r.getCode().equals(superClass)) {
									icd10Rubric.setModifiedBy(r.getModifiedBy());
									icd10Rubric.setEndRubric("m2");
									//break;
									//System.out.println("---" + code + " " + superClass + " " + r.getModifiedBy() + " " + icd10Rubric.getEndRubric());
								}	
							}
						}
						if (!excludeModifier.isEmpty()) {
							icd10Rubric.setExcludeModifier(excludeModifier);
							icd10Rubric.setEndRubric("e");
						} /* else {
							for (ICD10Rubric rex : icd10Rubrics) {
								if (!superClass.isEmpty() && rex.getCode().equals(superClass)) {
									icd10Rubric.setExcludeModifier(rex.getExcludeModifier());
								}
								//System.out.println(icd10Class.getExcludeModifier());
							}
						} */
						
						icd10Rubric.setCode(code);
						icd10Rubric.setVersion(version);
						icd10Rubric.setId(id);
						icd10Rubric.setKind(kind);
						icd10Rubric.setUsageKind(classUsageKind);
						icd10Rubric.setLang(lang);
						icd10Rubric.setFragmentType(fragmentType);
						icd10Rubric.setParaClass(paraClass);
						icd10Rubric.setSuperClass(superClass);
						icd10Rubric.setLabel(label);
						icd10Rubric.setRef(ref);
						icd10Rubrics.add(icd10Rubric);
						
						/*
						 * icd10Rubric reset !!! 
						 * don't use icd10Rubric = "";
						 */
						icd10Rubric = new ICD10Rubric();
						
						/*
						System.out.println(code + " "
								+ version + " "
								+ id + " "
								+ kind + " "
								+ classUsageKind + " "
								+ lang + " "
								+ fragmentType + " "
								+ paraClass + " "
								+ label + " "
								+ ref
								+ modifiedBy + " "
								+ excludeModifier 
								+ icd10Rubric.getEndRubric()
								);
						*/							
						classUsageKind="";
						fragmentType="";
						paraClass="";
						
						//superClass="";
						label="";
						
						ref="";
						isRef = false;
						isLeafElement = false;
					}
				
				}
			}
		} 
		
		////return e;	
	}
	
	public static String parsePara(Element e) {
		
		String para = "";
		
		if (e.attributeCount() == 1) {
			paraClass = e.attributeValue("class");
		}
	
		if (e.asXML().contains("Term")) {
			para = parseTerm(e);
			isTerm = true;
		} else {
			para = e.getText();
			isPara = true;
		}
		
		isPara = true;
		//System.out.println(code + " para : " + para);
		return para;
	}
	
	public static String parseList(Element e) {
		String list = "";
		String flag = "";

		if (e.attributeCount() == 1) {
			paraClass = e.attributeValue("class");
		}
	
		if ("loweralpha".equals(e.attributeValue("class"))) {
				list = "<ol type=\"a\">";
				flag = "ol";
		} else {
			list = "<ul>" ;
			flag = "ul";
		}
			
		for(Iterator<Node> i=e.nodeIterator(); i.hasNext();) {
			Node n = (Node) i.next();

			if (n.getNodeType() == 1) {
				Element e2 = (Element) n;	

				if ("ListItem".equals(e2.getName())) {
					for (Iterator<Element> j = e2.elementIterator(); j.hasNext();) {
						Element e3 = j.next();
						
						if ("Para".equals(e3.getName())) {
							if ("ul".equals(flag)) {
								list += "<li style=\"list-style-type:initial\">";
							} else {
								list += "<li style=\"list-style-type:lower-alpha\">";
							}
					
							list += parsePara(e3);
				
							list += "</li>";
						} else if ("List".equals(e3.getName())) {
							list += parseList(e3);
						}
					}
				}
			}
		}
		
		if ("ul".equals(flag)) {
			list += "</ul>";
		} else {
			list += "</ol>";
		}
		
		//System.out.println(list);
		isList=true;
		return list;
		
	}
	
	public static String parseTable(Element e) {
		String table = "";
		String flag = "";

		if (e.attributeCount() == 1) {
			paraClass = e.attributeValue("class");
		}
	
		table = "<Table border=\"1\">";

		for(Iterator<Node> i=e.nodeIterator(); i.hasNext();) {
			Node n = (Node) i.next();

			if (n.getNodeType() == 1) {
				Element e2 = (Element) n;	
				if ("THead".equals(e2.getName()) || "TBody".equals(e2.getName())) {
		
					if ("THead".equals(e2.getName())) {
						table += "<THead>";
						flag="THead";
					} else if ("TBody".equals(e2.getName())) {
						table += "<TBody>";
						flag="TBody";
					}
					
					for (Iterator<Element> j = e2.elementIterator(); j.hasNext();) {
						Element e3 = j.next();
						
						if ("Row".equals(e3.getName())) {
							table += "<tr>";
							
							for (Iterator<Element> k = e3.elementIterator(); k.hasNext();) {
								Element e4 = k.next();
								
								if ("Cell".equals(e4.getName())) { 
							
									if (e4.attributeCount() == 1) { 
										table += "<td style=\"margin:0.25em\"" 
												+ e4.attribute(0).getName() 
												+ "=" 
												+ "\"" 
												+ e4.attributeValue(e4.attribute(0).getName())
												+ "\">";												
									} else {
										table += "<td>";
									}

									for (Iterator<Element> l = e4.elementIterator(); l.hasNext();) {
											Element e5 = l.next();
											
											if ("Para".equals(e5.getName())) {
												table += "<p style=\"margin:0.25em\">" + parsePara(e5) + "</p>";
											} else if ("List".equals(e5.getName())) {
												table += parseList(e5);
											}
									}
								}
								table += "</td>";
							}
								
						}
						table += "</tr>";
					}
					if (flag.equals("THead")) {
						table += "</THead>";
					} else if(flag.equals("TBody")) {
						table += "</TBody>";
					} 
					
				}
			
			}
		}
		
		table += "</Table>";
		
		//System.out.println(table);
		isTable=true;
		return table;
		
	}
	
	public static String parseFragment(Element e) {

		int list_count=0;
		String list_content_head = "";
		String list_content = "";
		
		Element e2 = null;
		Iterator<Element> i_fragment = e.elementIterator();		
		for(list_count=0; i_fragment.hasNext(); list_count++) {
			e2 = i_fragment.next();
			
			if (list_count == 0) {
				fragmentType = e2.attributeValue("type");
				list_content_head = e2.getText() + " ";
			} else if (list_content_head != e2.getText() ) {
				list_content += ( e2.getText() + " ");
				parseRef(e2);
			}
		} 
		isFragment = true;
		return list_content_head + list_content;
	}
		
	public static String parseTerm(Element e) {
		String tmp = "";
		String value="";
		
		for(Iterator<Node> j=e.nodeIterator(); j.hasNext();) {
			Node n = (Node) j.next();
			//System.out.println("node : " + n.asXML());
			if (n.getNodeType() == 1) {
				Element e2 = (Element) n;					
				
				if (e2.getName() == "Term") {
					tmp = e2.attributeValue("class");
					if ( "tab".equals(tmp)) {
						value+= " " + e2.getText();
					} else if ("subscript".equals(tmp)) {
						value += "<Sub>" + e2.getText() + "</Sub>";
					} else if ("bold".equals(tmp)) {
						value += "<span style=\"font-weight:bold\">" + e2.getText() + "</span>"; 
					} else if ("italic".equals(tmp)) {
						value += "<span style=\"font-style:italic\">" + e2.getText() + "</span>"; 
					} else if ("Zinkl".equals(tmp)) {
						value += "<span style=\"font-weight:bold\">" + e2.getText() + "</span>"; 
					}
				} else if (e2.getName() == "Reference") {
					value += parseRef(e2);
				}
			} else if (n.getNodeType() == 3) {
				//System.out.println("tmp : " + tmp);
				value += n.getText();
			}
		}
		
		isTerm = true;
		value = value.trim();
		//System.out.println(code + " Term : " + value);
		return value;
	}
	
	public static String parseRef(Element e) {
		
		String value="";
		
		value = "<Reference class=\"" 
				+ e.attributeValue("class")
				+ (e.attributeValue("code") != null 
					? "\" code=\"" + e.attributeValue("code")
					: "")
				+ (e.attributeValue("usage") != null 
					? "\" usage=\"" + e.attributeValue("usage")
					: "")
				+ "\">"
				+ e.getText()
				+ "</Reference>\n";
		
		for(Iterator<Node> j=e.nodeIterator(); j.hasNext();) {
			Node n = (Node) j.next();
			if (n.getNodeType() == 1) {
				Element e2 = (Element) n;					
				if (e2.getName() == "Reference") {
					if (e2.attributeValue("usage") != null) {
						if ( "dagger".equals(e2.attributeValue("usage"))) {
							ref += (ref != "" ? ", " : "") + e2.getText() + "&dagger;";
						} else {
							ref += (ref != "" ? ", " : "") + e2.getText() + "*";
						}
					} else {
						ref += (ref != "" ? ", " : "") + e2.getText();
					}
				}
			}
		}
		
		if (isLabelRef && isPreferred) {
			labelRef = ref;
		}
		
		isRef = true;
		
		return value;
	}	
		
	public static void writeFile(File file1, File file2, boolean deleteFirst) throws IOException {

		ArrayList<String> modifier = new ArrayList<String>();
		ArrayList<String> modifier2 = new ArrayList<String>();
		
		String code="";
		String classKind = "";
		//String kind = "";
		String label1 = "";
		String label2 = "";
		
		if (deleteFirst) {
			file1.delete();
			file2.delete();
		}
		
//		ArrayList<ICD10Class> icd10Classes = new ArrayList<ICD10Class>();
		
		System.out.println("ClaMLParser: Writing File");

		BufferedWriter out1 = new BufferedWriter(new FileWriter(file1));
		BufferedWriter out2 = new BufferedWriter(new FileWriter(file2));

		out1.write(	
				"SEQ" + '\t' +
				"Code" + '\t' + 
				"Version" + '\t' + 
				"Class_Kind" + '\t' + 
				"Usage_Kind" + '\t' + 
				"Super_Class" + '\t' +
				"Label" + '\t' +
				"REF" + '\t' +
				"Children_count" + '\t' + 
				"Descendant_count" + '\t' +
				"PATH"
				);
		out1.newLine();
		
		/*
		out1.write(	
				"1" + '\t' +
				"ICD10 2019" + '\t' + 
				"2019" + '\t' + 
				"" + '\t' + 
				"" + '\t' + 
				"" + '\t' + 
				"" + '\t' +
				"22" + '\t' + 
				"" + '\t' +
				""
				);
		out1.newLine();
		*/
		
		Iterator<ICD10Class> i_class = icd10Classes.iterator();
		int seqClass = 0;
		for(; i_class.hasNext();) {
			ICD10Class icd10Class2 = i_class.next();
				seqClass++;
				out1.write(	
					String.valueOf(seqClass) + '\t'
					+ icd10Class2.getCode() + '\t' 
					+ icd10Class2.getVersion() + '\t' 
					+ icd10Class2.getClassKind() + '\t'  
					+ icd10Class2.getUsageKind() + '\t' 
					+ icd10Class2.getSuperClass() + '\t' 
					+ icd10Class2.getLabel() + '\t' 
					+ icd10Class2.getRef() + '\t'
					+ icd10Class2.getChildrenCount() + '\t' 
					+ icd10Class2.getDescendantCount() + '\t' 
					+ icd10Class2.getPath()
					);
				out1.newLine();
				
				//System.out.println(icd10Class2.getExcludeModifier());
				
				if (!icd10Class2.getExcludeModifier().isEmpty() && icd10Class2.getModifiedBy().isEmpty()) {
					///System.out.println("===\n" + " " + icd10Class2.getExcludeModifier() + " " + "\n===");
				} else if (!icd10Class2.getModifiedBy().isEmpty()) {
					code = icd10Class2.getCode();
					int codelength = (code.contains(".") ? code.length()-1 : code.length() );
					classKind = icd10Class2.getClassKind();
					modifier = icd10Class2.getModifiedBy();	
					label1 = icd10Class2.getLabel();
					for (String m : modifier ) {
						Iterator<ICD10ModifierClass> i_modifierClass = icd10ModifierClasses.iterator();
						for(; i_modifierClass.hasNext();) {
							ICD10ModifierClass icd10ModifierClass2 = i_modifierClass.next();
							//System.out.println(code + " " + String.valueOf(codelength+1) + " " +  m + " " + m.substring(m.length()-1) + " " + " " + icd10ModifierClass2.getModifier() + " " + icd10ModifierClass2.getModifier().substring(icd10ModifierClass2.getModifier().length()-1));
							//System.out.println(m + " " + icd10ModifierClass2.getModifier() + " " + icd10ModifierClass2.getKind());

							if ( (String.valueOf(codelength+1)
									.equals(icd10ModifierClass2.getModifier().substring(icd10ModifierClass2.getModifier().length()-1)))
									&& m.equals(icd10ModifierClass2.getModifier())) 
							{
								
								/*
								System.out.println(code + " " + classKind + " " 
										+ modifier + " " + icd10ModifierClass2.getModifier()
										+ " " + icd10ModifierClass2.getKind());
								*/
		
								if ( "preferred".equals(icd10ModifierClass2.getKind()) ) {
									seqClass++;
									out1.write(	
											String.valueOf(seqClass) + '\t'
											+ code + 
											( 
													(	(code.length() == 3) &&
															!icd10ModifierClass2.getCode().contains(".")
													) 
													? "." + icd10ModifierClass2.getCode()
													: icd10ModifierClass2.getCode() 
											)  + '\t' 
											+ icd10ModifierClass2.getVersion() + '\t' 
											+ classKind + '\t'  
											+ icd10ModifierClass2.getUsageKind() + '\t' 
											+ code + '\t' 
											+ label1 + " (" + icd10ModifierClass2.getLabel() + ")" + '\t' 
											+ icd10ModifierClass2.getRef() + '\t'
											+ icd10ModifierClass2.getChildrenCount() + '\t' 
											+ icd10ModifierClass2.getDescendantCount() + '\t' 
											+ icd10ModifierClass2.getPath()
											);
									out1.newLine();
								}
								
							}
						}
					}
				}
			
		}
		
		out2.write(	
				"SEQ" + '\t' +
				"Code" + '\t' + 
				"Version" + '\t' + 
				"Id" + '\t' +
				"Kind" + '\t' +
				"Modifier_Code" + '\t' +
				"Usage_Kind" + '\t' +
				"Lang" + '\t' +
				"Fragment_Type" + '\t' +
				"Para_Type" + '\t' +
				"Label" + '\t' +
				"REF"
				);
		out2.newLine();
		
		out2.write(	
				"1" + '\t' +
				"ICD10 2019" + '\t' + 
				"2019" + '\t' + 
				"" + '\t' +
				"" + '\t' +
				"" + '\t' +
				"" + '\t' +
				"en" + '\t' +
				"" + '\t' +
				"" + '\t' +
				"International Statistical Classification of Diseases and Related Health Problems 10th Revision" + '\t' +
				""
				);
		out2.newLine();
		
		/*
		 * write icd10Rubric to File
		 */
		int seqRubric = 1;
		ListIterator<ICD10Rubric> i_rubric = icd10Rubrics.listIterator();
		for(; i_rubric.hasNext();) {
			ICD10Rubric icd10Rubric2 = i_rubric.next();
			
			seqRubric++;
			out2.write(	
					String.valueOf(seqRubric) + '\t'
					+ icd10Rubric2.getCode() + '\t' 
					+ icd10Rubric2.getVersion() + '\t' 
					+ icd10Rubric2.getId() + '\t'  
					+ icd10Rubric2.getKind() + '\t' 
					+ "" + '\t' //modifier_code
					+ icd10Rubric2.getUsageKind() + '\t' 
					+ icd10Rubric2.getLang() + '\t' 
					+ icd10Rubric2.getFragmentType() + '\t'
					+ icd10Rubric2.getParaClass() + '\t' 
					+ icd10Rubric2.getLabel() + '\t' 
					+ icd10Rubric2.getRef() 
					/*
					+ '\t'
					+ icd10Rubric2.getModifiedBy() + '\t'
					+ icd10Rubric2.getExcludeModifier() + '\t'
					+ icd10Rubric2.getEndRubric() + '\t'
					*/
					);
			out2.newLine();
			/*
			System.out.println(
					String.valueOf(seqRubric) + '\t'
					+ icd10Rubric2.getCode() + '\t' 
					+ icd10Rubric2.getVersion() + '\t' 
					+ icd10Rubric2.getId() + '\t'  
					+ icd10Rubric2.getKind() + '\t' 
					+ icd10Rubric2.getUsageKind() + '\t' 
					+ icd10Rubric2.getLang() + '\t' 
					+ icd10Rubric2.getFragmentType() + '\t'
					+ icd10Rubric2.getParaClass() + '\t' 
					+ icd10Rubric2.getLabel() + '\t' 
					+ icd10Rubric2.getRef()
					);
			*/
			//System.out.println(icd10Rubric2.getCode() + " " + icd10Rubric2.getModifiedBy() + " " + icd10Rubric2.getEndRubric());

			code = icd10Rubric2.getCode();
			int codelength = (code.contains(".") ? code.length()-1 : code.length() );
			label2 = icd10Rubric2.getLabel();
			modifier2 = icd10Rubric2.getModifiedBy();
			
			/*
			 * write icd10Modifier to File (superclass for example. V01-X59)
			 */
			
			if (icd10Rubric2.getEndRubric().equals("end")) {
				//System.out.println("--- modifier : " + modifier + "  Rubric modifier : " + icd10Rubric2.getModifiedBy());
				/*
				  
				if (!icd10Rubric2.getExcludeModifier().isEmpty() && icd10Rubric2.getModifiedBy().isEmpty()) {
					System.out.println("---\n" + " " + icd10Rubric2.getExcludeModifier() + " " + "\n---");
					;
				} else if (!icd10Rubric2.getModifiedBy().isEmpty() ) {
				*/	
				int k_int = -1;
				for (String m2 : modifier2 ) {
					Iterator<ICD10Modifier> i_modifier = icd10Modifiers.iterator();
					k_int++;
					for(; i_modifier.hasNext();) {
						ICD10Modifier icd10Modifier2 = i_modifier.next();
						//System.out.print(code + " " + String.valueOf(codelength+1) + " ");
						//System.out.print(icd10Modifier2.getCode() + " ");
						//System.out.println(code + modifier2 + " " + m2 + " " + icd10Modifier2.getCode());
						if ( m2.equals(icd10Modifier2.getCode())) {
							seqRubric++;								
							out2.write(
									String.valueOf(seqRubric) + '\t'
									+ code + '\t' 
									+ icd10Modifier2.getVersion() + '\t' 
									+ icd10Modifier2.getId() + '\t'  
									+ icd10Modifier2.getKind() + (k_int == 0 ? "" : k_int) + '\t' 
									+ "" + '\t' // modifier_code
									+ icd10Modifier2.getUsageKind() + '\t' 
									+ icd10Modifier2.getLang() + '\t' 
									+ icd10Modifier2.getFragmentType() + '\t'
									+ icd10Modifier2.getParaClass() + '\t' 
									+ icd10Modifier2.getLabel() + '\t' 
									+ icd10Modifier2.getRef()
									);
							out2.newLine();
							//System.out.println(seqRubric + " " + code + " " + m2 + " " + icd10Modifier2.getCode() + " " + icd10Modifier2.getLabel());
							
						}
					}
					
				
				int m_int = -1;
				for (String m3 : modifier2) {
					Iterator<ICD10ModifierClass> i_modifierClass2 = icd10ModifierClasses.iterator();
					m_int++;
					for (; i_modifierClass2.hasNext();) {
						ICD10ModifierClass icd10ModifierClass2 = i_modifierClass2.next();
							if (/*String.valueOf(codelength).equals(icd10ModifierClass2.getModifier().substring(icd10ModifierClass2.getModifier().length()-1))
							&& */m2.equals(m3) && m3.equals(icd10ModifierClass2.getModifier())) {
								seqRubric++;								
								out2.write(
										String.valueOf(seqRubric) + '\t'
										+ code + '\t' 
										+ icd10ModifierClass2.getVersion() + '\t' 
										+ icd10ModifierClass2.getId() + '\t'
										+ "modifier" + (m_int == 0 ? "" : m_int) + '\t'
										+ icd10ModifierClass2.getCode() + '\t' // modifier_code
										+ icd10ModifierClass2.getUsageKind() + '\t' 
										+ icd10ModifierClass2.getLang() + '\t' 
										+ "" + '\t'
										+ icd10ModifierClass2.getKind()  + '\t'   
										+ icd10ModifierClass2.getLabel() + '\t' 
										+ icd10ModifierClass2.getRef()
										);
								out2.newLine();

							}
						
					}
				
				}
				}	
			}	
			
			/*
			 * write icd10ModifierClass to File (subclass expansion)
			 */
			
			if ((icd10Rubric2.getEndRubric().equals("end") || icd10Rubric2.getEndRubric().equals("end2")) 
					&& !icd10Rubric2.getModifiedBy().isEmpty() && icd10Rubric2.getExcludeModifier().isEmpty()) { //!modifier2.isEmpty()) {
				for (String m2 : modifier2 ) {
					Iterator<ICD10ModifierClass> i_modifierClass = icd10ModifierClasses.iterator();
					for(; i_modifierClass.hasNext();) {
						ICD10ModifierClass icd10ModifierClass2 = i_modifierClass.next();

						//System.out.print(code + " " + String.valueOf(codelength+1) + " ");
						//System.out.print(icd10Modifier2.getCode() + " ");
						//System.out.println(icd10Modifier2.getCode().substring(icd10Modifier2.getCode().length()-1));
						if ( (String.valueOf(codelength+1)
								.equals(icd10ModifierClass2.getModifier().substring(icd10ModifierClass2.getModifier().length()-1)))
								&& m2.equals(icd10ModifierClass2.getModifier())) {
							
									seqRubric++;
									/*
									System.out.println(code + " " + icd10ModifierClass2.getCode() + " " +
									( 
											(	(code.length() == 3) &&
													!icd10ModifierClass2.getCode().contains(".")
											) 
											? "." + icd10ModifierClass2.getCode()
											: icd10ModifierClass2.getCode() 
									)
									);
									*/
									out2.write(	
											String.valueOf(seqRubric) + '\t'
											+ code + 
											( 
													(	(code.length() == 3) &&
															!icd10ModifierClass2.getCode().contains(".")
													) 
													? "." + icd10ModifierClass2.getCode()
													: icd10ModifierClass2.getCode() 
											)  + '\t' 
											+ icd10ModifierClass2.getVersion() + '\t' 
											+ icd10ModifierClass2.getId() + '\t'  
											+ icd10ModifierClass2.getKind() + '\t' 
											+ "" + '\t'
											+ icd10ModifierClass2.getUsageKind() + '\t' 
											+ icd10ModifierClass2.getLang() + '\t' 
											+ icd10ModifierClass2.getFragmentType() + '\t'
											+ icd10ModifierClass2.getParaClass() + '\t' 
											//+ label2 + " (" + icd10ModifierClass2.getLabel() + ")" + '\t' 
											+ icd10ModifierClass2.getLabel() + '\t' 				
											+ icd10ModifierClass2.getRef()
											);
									out2.newLine();
									//System.out.println(seqRubric + " " + code + icd10ModifierClass2.getCode() + " " + icd10ModifierClass2.getKind() +" " + m2 + " " +  " " + icd10ModifierClass2.getLabel());
									
								
						}
					}
					
				}
			}
			
		}

		out1.close();
		out2.close();

	} 
	
}
