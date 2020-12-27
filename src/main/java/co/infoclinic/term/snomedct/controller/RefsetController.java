package co.infoclinic.term.snomedct.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import co.infoclinic.term.common.utils.PropertiesUtil;
import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.service.RefsetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Referenceset API를 제공하는 컨트롤러
 */
@Api(value = "Referenceset", description = "Referenceset", tags = QryApi.API_TAGS_REFERENCESET)
@RestController(value = "SCTRefsetCtrl")
public class RefsetController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(RefsetController.class);

	/** DI: Referenceset service */
	@Autowired
	private RefsetService refsetSvc;


	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/// ----------------------------------------
	/// 조회
	/// ----------------------------------------
	
	
	/**
	 * 
	 * @param release
	 * @param hasmbrs
	 * @param version
	 * @return
	 */
	@ApiOperation(value = "Get Referenceset List")
	@RequestMapping(value = "/refsets/SNOMEDCT", method = RequestMethod.GET)
	public Object getRefsetList(
			@RequestParam(value = "release", required = true, defaultValue = "itn") String release,
			@RequestParam(value = "hasmbrs", required = false, defaultValue = "false") boolean hasmbrs) {
		Object obj = null;
		
		// 국제배포판 레퍼런스세트 일 경우
		if ("itn".equals(release)) {
			// 멤버수가 존재하는 세트 목록만 원하는 경우 : 모든 세트 목록만 원하는 경우
			obj = (List<String>) (hasmbrs ? refsetSvc.getReferencesetIdExistList():refsetSvc.getReferencesetIdList());
		} else if ("user".equals(release)) {
			obj = (String) getUserRefsetList();
		} else {
			// not found release
			log.error("Not Found Release: {}", release);
		}
		
		return obj;
	}

	
	
	
	/// ----------------------------------------
	/// 추가, 수정, 삭제
	/// ----------------------------------------
	
	/**
	 * 사용자 레퍼런스세트 계층구조를 저장
	 * 
	 * consumes: 헤더 값중 Content-Type
	 * produces: 헤더 값중 Accept
	 * 
	 * @return
	 * @throws IOException 
	 */
	@ApiOperation(value = "Set Referenceset List (User Extension)")
	@RequestMapping(value = "/refset/SNOMEDCT", params = "release=user", method = RequestMethod.POST)
	public String setUserRefsetList(
			@RequestBody String json,
			@RequestParam(value = "release", required = true) String release
		) throws IOException {

		PropertiesUtil prop = new PropertiesUtil();
		String refsetPath = prop.getPropValue("sct.refset.dir").toString();
		
		File f = new File(refsetPath + "/admin/userTree.json");
		FileOutputStream output = new FileOutputStream(f);
	
		if (f.createNewFile()) {
			// create file
		}
		//else {
			// already file
		//}
		
		output.write(json.getBytes());
		
		output.close();
		
		return json;
	}
	
	

	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	
	/**
	 * 사용자 레퍼런스세트 계층구조를 반환
	 * 
	 * @return
	 */
	private String getUserRefsetList() {

		StringBuilder sb = new StringBuilder();
		Gson gson = new Gson();
		JsonElement json;
		
		PropertiesUtil prop = null;
		
		try {
			prop = new PropertiesUtil();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String refsetPath = prop.getPropValue("sct.refset.dir").toString();

		try {
			//InputStream in = this.getClass().getClassLoader().getResourceAsStream("urs.json"); // new
			InputStream in = new FileInputStream(new File(refsetPath + "/admin/userTree.json"));											
																			
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			String line;

			while ((line = r.readLine()) != null) {
				sb.append(line);
			}
			r.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		json = gson.fromJson(sb.toString(), JsonElement.class);
		return gson.toJson(json);
	}
}
