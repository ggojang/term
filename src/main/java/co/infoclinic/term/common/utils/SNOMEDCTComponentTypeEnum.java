package co.infoclinic.term.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * SNOEMD CT Component Type Enum
 */
public enum SNOMEDCTComponentTypeEnum {
	CONCEPT("00", "10"), DESCRIPTION("01", "11"), RELATIONSHIP("02", "12"), UNKNOWN("", "");

	/** SCTID Short Type */
	private final String shortType;
	
	/** SCTID Long Type */
	private final String longType;

	
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/**
	 * Enum 반환 (Type에 의한)
	 * 
	 * @param partitionId
	 * @return
	 */
	public static SNOMEDCTComponentTypeEnum getByPartitionId(String partitionId) {
		// 숫자형식 여부
		boolean isNumeric = StringUtils.isNumericSpace(partitionId);
		// PartitionId 길이(2자리) 만족 여부
		boolean isPrtIdLen = partitionId == null ? false : (partitionId.length() == 2 ? true : false);

		// 숫자형식이 아니거나 숫자형식이지만 파티션아이디 길이를 만족하지 않는 경우 UNKNOWN 반환
		if (!isNumeric || (isNumeric && !isPrtIdLen)) {
			return UNKNOWN;
		}

		if (CONCEPT.getShortType().equals(partitionId) || CONCEPT.getLongType().equals(partitionId)) {
			return CONCEPT;
		} else if (DESCRIPTION.getShortType().equals(partitionId) || DESCRIPTION.getLongType().equals(partitionId)) {
			return DESCRIPTION;
		} else if (RELATIONSHIP.getShortType().equals(partitionId) || RELATIONSHIP.getLongType().equals(partitionId)) {
			return RELATIONSHIP;
		}
		return UNKNOWN;
	}

	
	/**
	 * Enum 반환 (ID에 의한)
	 * 
	 * @param id
	 * @return
	 */
	public static SNOMEDCTComponentTypeEnum getById(String id) {
		// 숫자형식 여부
		boolean isNumeric = StringUtils.isNumericSpace(id);
		// SCTID 길이(6~18자리) 만족 여부
		boolean isSctIdLen = !StringUtils.isEmpty(id) ? (id.matches("^(\\d{6,18})$") ? true:false):false;

		if (!isNumeric || (isNumeric && !isSctIdLen)) {
			return UNKNOWN;
		}

		int idLength = id.length();
		String partitionId = id.substring(idLength - 3, idLength - 1);

		return getByPartitionId(partitionId);
	}

	
	/**
	 * Short 타입 반환
	 * 
	 * @return
	 */
	public String getShortType() {
		return shortType;
	}

	
	/**
	 * Long 타입 반환
	 * 
	 * @return
	 */
	public String getLongType() {
		return longType;
	}

	
	/**
	 * 유효한 아이디인지 확인
	 * 
	 * @param id
	 * @return
	 */
	public static boolean isValidIdentifier(String id) {
		return getById(id) != UNKNOWN ? true:false;
	}

	
	
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------

	/**
	 * 생성자
	 * 
	 * @param shortType
	 * @param longType
	 */
	private SNOMEDCTComponentTypeEnum(String shortType, String longType) {
		this.shortType = shortType;
		this.longType = longType;
	}

}
