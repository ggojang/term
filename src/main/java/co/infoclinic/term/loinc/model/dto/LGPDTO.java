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
public class LGPDTO {

	/** Parent Group Id : LPxxxxxx-x */
	private String parentLGId;
	
	/** Parent Group Name :  */
	private String parentLG;
	
	/** Status */
	private String status;
}