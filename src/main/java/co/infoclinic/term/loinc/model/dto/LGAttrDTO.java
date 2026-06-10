package co.infoclinic.term.loinc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
/** 
 * @EqualsAndHashCode(callSuper = true)
 * @author seungjongyu
 *
 */
@NoArgsConstructor
@AllArgsConstructor
public class LGAttrDTO {
	
	/** Parent Group Id : LPxxxxxx-x */
	private String parentLGId;
	
	/** LOINC Common Name :  */
	private String LGId;
	
	/** LOINC Part Number : LPxxxxxx-x */
	private String type;
	
	/** Part Name */
    private String value;
}