package co.infoclinic.term.loinc.utils;

/**
 * Part Enum
 */
public enum PartEnum {
	COMPONENT("COMPONENT"), 
	PROPERTY("PROPERTY"),
	TIME("TIME_ASPECT"),
	SYSTEM("SYSTEM"),
	SCALE("SCALE_TYPE"),
	METHOD("METHOD_TYPE");
	
	private final String colName;
	
	private PartEnum(String colName) {
		this.colName = colName;
	}
	
	
	/**
	 * Part의 컬럼명 반환
	 * @return
	 */
	public String getColName() {
		return this.colName;
	}
}
