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
public class LPMapDTO {
	
	/** Part Number */
	private String partNumber;
	
	/** Part Name */
    private String partName;
	
    /** Part Type Name */
    private String partTypeName;
	
    /** Ext Code Id */
	private String extCodeId;
	
    /** Ext Code Display Name */
	private String extCodeDisplayName;
	
    /** Ext Code System */
	private String extCodeSystem;
	
    /** Equivalence */
	private String equivalence;
	
    /** Content Origin */
	private String contentOrigin;
	
    /** Ext Code System Version */
	private String extCodeSystemVersion;
	
    /** Ext Code System Copyright Notice */
	private String extCodeSystemCopyrightNotice;
}