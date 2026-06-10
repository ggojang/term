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
public class LPDTO {

	/** Part Number */
	private String partNumber;
	
	/** Part Type Name */
    private String partTypeName;

	/** Part Name */
    private String partName;

    /** Part Display Name */
    private String partDisplayName;

    /** Status */
    private String status;
	
}
