package co.infoclinic.claml;

import java.util.Iterator;

import org.dom4j.Element;

public class ClaMLRoot {
	
	String clamlVersion = new String();
	String topLevelSort = new String();
	String lang = new String();
	String idAuthority = new String();
	String idUid = new String();
	String icd10Title = new String();
	String icd10Date = new String();
	String icd10Name = new String();
	String icd10Version = new String();	
	
	public ClaMLRoot(Element element) {
		/*
		<ClaML version="2.0.0">
		 */
	
		clamlVersion = element.attributeValue("version"); 
			
		/*
    	<Meta name="TopLevelSort" value="I II III IV V VI VII VIII IX X XI XII XIII XIV XV XVI XVII XVIII XIX XX XXI XXII"/>
    	<Meta name="lang" value="en"/>
    	<Identifier authority="WHO" uid="icd10_2016"/>
    	<Title date="2014-10-14" name="ICD-10-EN-2016" version="2016">International Statistical Classification of Diseases and Related Health Problems 10th Revision</Title>
		 */
		
		Element e = null;
		Iterator<Element> elements = element.elementIterator();
		
		e = elements.next();
		topLevelSort = e.attributeValue("value");
		
		e = elements.next();
		lang = e.attributeValue("value");
		
		e = elements.next();
		idAuthority = element.attributeValue("authority");
		idUid= element.attributeValue("uid");
		
		e = elements.next();
		icd10Title = element.element("Title").getText();
		icd10Date = element.element("Title").attributeValue("date");
		icd10Name = element.element("Title").attributeValue("name");
		icd10Version = element.element("Title").attributeValue("version");
			
		return;
	}

	public String getClamlVersion() {
		return clamlVersion;
	}

	public void setClamlVersion(String clamlVersion) {
		this.clamlVersion = clamlVersion;
	}

	public String getTopLevelSort() {
		return topLevelSort;
	}

	public void setTopLevelSort(String topLevelSort) {
		this.topLevelSort = topLevelSort;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getIdAuthority() {
		return idAuthority;
	}

	public void setIdAuthority(String idAuthority) {
		this.idAuthority = idAuthority;
	}

	public String getIdUid() {
		return idUid;
	}

	public void setIdUid(String idUid) {
		this.idUid = idUid;
	}

	public String getIcd10Title() {
		return icd10Title;
	}

	public void setIcd10Title(String icd10Title) {
		this.icd10Title = icd10Title;
	}

	public String getIcd10Date() {
		return icd10Date;
	}

	public void setIcd10Date(String icd10Date) {
		this.icd10Date = icd10Date;
	}

	public String getIcd10Name() {
		return icd10Name;
	}

	public void setIcd10Name(String icd10Name) {
		this.icd10Name = icd10Name;
	}

	public String getIcd10Version() {
		return icd10Version;
	}

	public void setIcd10Version(String icd10Version) {
		this.icd10Version = icd10Version;
	}
	
}
