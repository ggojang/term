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
public class LGTermDTO {

	private String category;
	
	private String LGId;

    private String archetype;

    private String LoincNumber;
    
    private String LongCommonName;
    
    //추가
	private String LGIdName;
}