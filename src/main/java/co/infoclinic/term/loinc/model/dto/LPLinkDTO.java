package co.infoclinic.term.loinc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 
 * @EqualsAndHashCode(callSuper = true)
 * @author seungjongyu
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LPLinkDTO {
	
	/** LOINC Number : xxxxxx-x */
	private String loincNumber;
	
	/** LOINC Common Name :  */
	private String longCommonName;
	
	/** LOINC Part Number : LPxxxxxx-x */
	private String partNumber;
	
	/** Part Name */
	private String partName;

	/** Part Code System */
	private String partCodeSystem;
    
	/** Part Type Name */
	private String partTypeName;

	/** Link Type Name */
	private String linkTypeName;
	
	/** Property */
	private String property;
}