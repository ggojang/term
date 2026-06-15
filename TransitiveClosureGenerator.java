package co.infoclinic.term.snomedct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransitiveClosureGenerator {

	/** The Root SNOMEDCT ID. */
	private static final String ROOT_SCTID = "138875005";

	/** The terminated char **/
	// private static final String DELIMITER = "	";
	private static final String DELIMITER = "\t";

	// 코드 & 자식코드 목록 맵
	private Map<String, Set<String>> codeChildCodesMap = new HashMap<String, Set<String>>();

	// 코드 & 용어 맵
	private Map<String, String> codeTermMap = new HashMap<String, String>();

	// 코드 & 하위코드 목록 맵
	private Map<String, Set<String>> codeDescendantCodesMap = new HashMap<String, Set<String>>();

	/** The relationships file. */
	private String relFilePath;

	/** The output file. */
	private String outFilePath;

	private BufferedWriter out;

	private BufferedReader in;

	private int pathCount = 0;

	/**
	 * Instantiates an empty {@link TransitiveClosureGenerator}.
	 */
	public TransitiveClosureGenerator() {}

	/**
	 * Sets the relationships file.
	 *
	 * @param relFilePath the relationships file
	 */
	public void setRelationshipsFile(String relFilePath) {
		this.relFilePath = relFilePath;
	}

	/**
	 * Sets the output file.
	 *
	 * @param outFilePath the output file
	 */
	public void setOutputFile(String outFilePath) {
		this.outFilePath = outFilePath;
	}

	/**
	 * Compute the transitive closure file.
	 *
	 * @throws Exception the exception
	 */
	public void compute() throws Exception {
		// Check assumptions/prerequisites
		long startTime = System.currentTimeMillis();
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Transitive Closure 생성 시작 ... " + new Date());

		// Check Relationship File Path
		if (relFilePath == null) {
			throw new Exception("오류 메시지: Relationship 파일에 문제가 발생했습니다.");
		}
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "  원본 Relationship 파일 = " + relFilePath);

		// Check Out File Path
		if (outFilePath == null) {
			throw new Exception("오류 메시지: 출력할 파일에 문제가 발생했습니다.");
		}
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "  출력할 파일 = " + outFilePath);

		// Create File Object for Relationship
		File rf = new File(relFilePath);
		if (!rf.exists()) {
			throw new Exception("오류 메시지: Relationship 파일이 존재하지 않습니다.");
		}

		// Create File Object for Out
		File of = new File(outFilePath);
		if (of.exists()) {
			throw new Exception("오류 메시지: 출력할 파일이 이미 존재합니다. 파일을 지운 후 다시 시도해 주세요.");
		}

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "  원본 Relationship 로딩 시작 ... " + new Date());

		// Create Buffer(Reader/Writer)
		out = new BufferedWriter(new FileWriter(outFilePath, true));
		in = new BufferedReader(new FileReader(rf));

		int lineCount = 0;
		Set<String> children;
		String line;
		while ((line = in.readLine()) != null) {
			// String[] tokens = line.split(",");
			String[] tokens = line.split("\t");
			String chd = null;
			String par = null;
			String term = null;

			chd = tokens[0];
			par = tokens[1];
			term = tokens[2];

			// Add par/chd relationship
			if (par == null || par.isEmpty()) {
				throw new Exception("Empty parent " + line);
			}
			if (chd == null || chd.isEmpty()) {
				throw new Exception("Empty child " + line);
			}
			if (term == null || term.isEmpty()) {
				throw new Exception("Empty term " + line);
			}

			if (codeChildCodesMap.containsKey(par)) {
				// 부모코드가 codeChildCodesMap에 이미 등록되어있는 경우
				children = codeChildCodesMap.get(par);
			} else {
				// 부모코드가 codeChildCodesMap에 등록되어있지 않은 경우
				children = new HashSet<String>();
				codeChildCodesMap.put(par, children);
			}

			// 자식 코드 추가
			children.add(chd);
			// 용어 추가
			codeTermMap.put(chd, term);

			lineCount++;
		}

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "     원본 총 라인 수  = " + lineCount);
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "    Transitive Closure 연산 시작 ... " + new Date());


		travelSubtypes();

		closeFiles();

		long endTime = System.currentTimeMillis();
		long totalTime = (endTime - startTime) / 1000;

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "    Transitive Closure 연산 종료 ... " + new Date());
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "완료: 생성된 Path수 = " + pathCount + " / 소요시간 = " + totalTime + "초");
	}

	/**
	 * Close files
	 */
	private void closeFiles() {
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addSubtypeCode(String parentPath, String code) {
		Set<String> descCodes;
		String[] parentCodes = parentPath.split("~");
		for (String parentCode : parentCodes) {
			if (codeDescendantCodesMap.containsKey(parentCode)) {
				descCodes = codeDescendantCodesMap.get(parentCode);
			} else {
				descCodes = new HashSet<String>();
				codeDescendantCodesMap.put(parentCode, descCodes);
			}

			descCodes.add(code);
		}
	}

	/**
	 * Get term of code
	 *
	 * @param code
	 */
	private String getTerm(String code) {
		if (code == null) {
			return "";
		}

		String term = "";
		if (codeTermMap.containsKey(code)) {
			term = codeTermMap.get(code);
		}

		return term;
	}

	/**
	 * Get descendant count of code
	 * @param code
	 */
	private int getDescendantCount(String code) {
		return codeDescendantCodesMap.containsKey(code) ? codeDescendantCodesMap.get(code).size() : 0;
	}


	/**
   * Write to File Buffer
	 *
	 * @param code
	 * @param parentCode
	 * @param term
	 * @param childrenCount
	 * @param descendantCount
	 * @param depth
	 * @param path
	 */
	private void write(String code, String parentCode, String term, int childrenCount, int descendantCount, int depth, String path) throws IOException {
		out.write(
			"\"" + code + "\"" + DELIMITER +             // code
			"\"" + parentCode + "\""  + DELIMITER +       // parent code of code
			"\"" + term + "\""  + DELIMITER +             // term of code
			"\"" + childrenCount + "\""  + DELIMITER +    // children count of code
			"\"" + descendantCount + "\""  + DELIMITER +  // descendant count of code
			"\"" + depth + "\""  + DELIMITER +            // depth from root code
			"\"" + path + "\""  +                         // path from root code
			"\r\n"                         // new line
		);
		pathCount++;
	};

	/**
	 * Travel subtypes
	 * @throws IOException
	 */
	private void travelSubtypes() throws IOException {
		Set<String> childCodes = null;
		String term;
		String path;
		int childrenCount = 0;
		int descendantCount = 0;
		int depth = 1;
		// 부모가 138875005인 자식 목록
		Set<String> codes = codeChildCodesMap.get(ROOT_SCTID);
		for (String code : codes) {
			addSubtypeCode(ROOT_SCTID, code);

			// code: child of ROOT_SCTID
			if (codeChildCodesMap.containsKey(code)) {
				childCodes = codeChildCodesMap.get(code);
				childrenCount = childCodes.size();
				path = concatPath(ROOT_SCTID, code);

				travelSubtypes(code, path, childCodes, depth);
			}
			term = getTerm(code);
			descendantCount = getDescendantCount(code);

			// 코드 | 부모 | 용어 | 자식 수 | 자손 수 \ 깊이 | 경로
			write(code, ROOT_SCTID, term, childrenCount, descendantCount, depth, ROOT_SCTID);
		}

		write(ROOT_SCTID, "", "SNOMED CT Concept (SNOMED RT+CTV3)", codes.size(), getDescendantCount(ROOT_SCTID), 0, "");
	}


	/**
	 * Compute paths.
	 *
	 * @param parentCode the parent
	 * @param parentPath the parent path
	 * @param codes the code list
	 * @param depth the depth from root
	 * @throws IOException
	 */
	private void travelSubtypes(String parentCode, String parentPath, Set<String> codes, int depth) throws IOException {
		int childrenCount = 0;
		int descendantCount = 0;
		String term;
		String path;
		Set<String> childCodes;

		for (String code : codes) {
			addSubtypeCode(parentPath, code);

			if (codeChildCodesMap.containsKey(code)) {
				childCodes = codeChildCodesMap.get(code);
				childrenCount = childCodes.size();
				path = concatPath(parentPath, code);
				travelSubtypes(code, path, childCodes, depth + 1);
			} else {
				childrenCount = 0;
			}

			term = getTerm(code);
			descendantCount = getDescendantCount(code);

			// 코드 | 부모 | 용어 | 자식 수 | 자손 수 \ 깊이 | 경로
			write(code, parentCode, term, childrenCount, descendantCount, (depth + 1), parentPath);
		}
	}


	/**
	 * Concat parent path and code
	 *
	 * @param parentPath
	 * @param code
	 */
	private String concatPath(String parentPath, String code) {
		return parentPath + "~" + code;
	}


	/**
	 * Application entry point. The first parameter should be the path to a
	 * snapshot inferred RF2 relationships or RF1 relationships file. The second
	 * parameter should be the output filename and should not yet exist.
	 *
	 * @param argv the command line arguments
	 */
	public static void main(String[] argv) {
		try {
			TransitiveClosureGenerator generator = new TransitiveClosureGenerator();
			generator.setRelationshipsFile(argv[0]);
			generator.setOutputFile(argv[1]);
			generator.compute();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
}

