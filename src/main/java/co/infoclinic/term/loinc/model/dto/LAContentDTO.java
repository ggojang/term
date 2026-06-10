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
public class LAContentDTO {
	
	private String LAID;	
	private String LAName;	
	private String LAOid;	
    private String extDefinedYn;    
    private String extDefinedLACodeSystem;    
	private String extDefinedLALink;
	private String answerStringID;	
    private String localAnswerCode;    
    private String localAnswerCodeSystem;	
	private int	   sequenceNumber;	
	private String displayText;	
    private String extCodeID;    
    private String extCodeDisplayName;    
	private String extCodeSystem;
	private String extCodeSystemVersion;	
    private String extCodeSystemCopyrightNotice;     
    private String subsequenceTextPrompt;
	private String description;	
    private String score;
}