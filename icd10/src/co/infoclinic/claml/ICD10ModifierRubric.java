package co.infoclinic.claml;

public class ICD10ModifierRubric {
	String code="";
	String version="";
	String id="";
	String kind="";
	String usageKind="";
	String lang="";
	String fragmentType="";
	String paraClass="";
	String label="";
	String ref="";

	public ICD10ModifierRubric() {
		super();
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getUsage_Kind() {
		return usageKind;
	}

	public void setUsage_Kind(String usageKind) {
		this.usageKind = usageKind;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getFragment_type() {
		return fragmentType;
	}

	public void setFragment_type(String fragmentType) {
		this.fragmentType = fragmentType;
	}

	public String getParse_type() {
		return paraClass;
	}

	public void setParse_type(String paraClass) {
		this.paraClass = paraClass;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}
}
