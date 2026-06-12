package co.infoclinic.term.snomedct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MrcmAppender {
   
    /** The relationships file. */
    private String relationshipsFile;
    
    /** The mrcm file. */
    private String mrcmFile;

    /** The index file. */
    private String indexFile;

    /** The output file. */
    private String outputFile;
    
    /** The lines terminated char **/
    private String lineChar = "\t";

    private String root = "138875005";    

    private int lineCount = 0;
    
    private Map<String, Set<String>> rangeAttrMap;
    
    private Map<String, Set<String>> conceptRuleMap = new HashMap<String, Set<String>>();
    
    /**
     * Instantiates an empty {@link TransitiveClosureGenerator}.
     */
    public MrcmAppender() {
        // do nothing
    }
    
    /**
     * Sets the relationships file.
     *
     * @param relationshipsFile the relationships file
     */
    public void setRelationshipsFile(String relationshipsFile) {
        this.relationshipsFile = relationshipsFile;
    }
    
    /**
     * Sets the mrcm file.
     *
     * @param mrcmFile the mrmcm file
     */
    public void setMrcmFile(String mrcmFile) {
        this.mrcmFile = mrcmFile;
    }

    /**
     * Sets the index file.
     *
     * @param indexFile the index file
     */
    public void setIndexFile(String indexFile) {
        this.indexFile = indexFile;
    }

    /**
     * Sets the output file.
     *
     * @param outputFile the output file
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }
    
    private void log(String msg) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, msg);
    }
    
    /**
     * Compute the transitive closure file.
     *
     * @throws Exception the exception
     */
    public void compute() throws Exception {
        long startTime = System.currentTimeMillis();

        if (relationshipsFile == null) {
            throw new Exception("오류 메시지: Relationship 파일에 문제가 발생했습니다.");
        }
        log("  원본 Relationship 파일 = " + relationshipsFile);
        
        if (outputFile == null) {
            throw new Exception("오류 메시지: 출력할 파일에 문제가 발생했습니다.");
        }
        log(" 출력할 파일 = " + outputFile);
        
        File rf = new File(relationshipsFile);
        if (!rf.exists()) {
            throw new Exception("오류 메시지: Relationship 파일이 존재하지 않습니다.");
        }
        
        File idxf = new File(indexFile);
        if (!idxf.exists()) {
            throw new Exception("오류 메시지: Index 파일이 존재하지 않습니다.");
        }
        
        File of = new File(outputFile);
        if (of.exists()) {
            throw new Exception("오류 메시지: 출력할 파일이 이미 존재합니다. 파일을 지운 후 다시 시도해 주세요.");
        }

        @SuppressWarnings("resource")
        BufferedReader in = new BufferedReader(new FileReader(rf));
        
        String line;
        Map<String, Set<String>> parChd = new HashMap<>();
        int ct = 0;
        
        while ((line = in.readLine()) != null) {
            String[] tokens = line.split(lineChar);
            
            String chd = null;
            String par = null;
            
            chd = tokens[0];
            par = tokens[1];
            // Add par/chd relationship
            if (par == null || par.isEmpty()) {
                throw new Exception("Empty parent " + line);
            }
            if (chd == null || chd.isEmpty()) {
                throw new Exception("Empty child " + line);
            }
            
            // Handle par/chd
            if (!parChd.containsKey(par)) {
                parChd.put(par, new HashSet<String>());
            }
            Set<String> children = parChd.get(par);
            children.add(chd);
            
            ct++;
        }
        
        log("     원본 총 라인 수  = " + ct);
        
        //
        // Write transitive closure file
        //
        ct = 0;
        log(String.valueOf(parChd.containsKey(root)));
        Set<String> chd = parChd.get(root);
        Set<String> c;
        Set<String> ruleSet;
        
        for (String code : chd) {
            ruleSet = appendRule(code, new HashSet<String>());
            checkAndSaveRule(code, ruleSet);
            
            // code: child of root
            if (parChd.containsKey(code)) {
                c = parChd.get(code);
                travelSubtypes(code, c, parChd, ruleSet);
            }
            
            lineCount++;
        }
        
        log("    IndexWithMrcm 연산 종료 ... " + new Date());
        
        in.close();
        
        
        Map<String, String> crMap = new HashMap<String, String>();
        for (Map.Entry<String, Set<String>> r : conceptRuleMap.entrySet()){
          String p = "";
          for (String s:r.getValue()) {
            p += "+" + s;
          }
          crMap.put(r.getKey(), p);
        }

        @SuppressWarnings("resource")
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFile, true));
        @SuppressWarnings("resource")
        BufferedReader idxIn = new BufferedReader(new FileReader(idxf));
        while ((line = idxIn.readLine()) != null) {
            String[] tokens = line.split(lineChar);
            
            String conceptId = null;
            String componentActive = "";
            String mrcm = "";
            
            // concept id
            conceptId = tokens[0];
            componentActive = tokens[6];
            
            // concept active && description active
            if ("1".equals(componentActive)) {
              mrcm = crMap.containsKey(conceptId) ? crMap.get(conceptId):"";
            }

            out.write(line + lineChar + mrcm + "\r\n");
	}

        idxIn.close();
	out.close();
        
        long endTime   = System.currentTimeMillis();
        long totalTime = (endTime - startTime) / 1000;
        log("완료: Attribute 대상 컨셉 수(root제외) = " + conceptRuleMap.size() + " / 소요시간 = " + totalTime + "초");
    }
    
    private Set<String> appendRule(String id, Set<String> ruleSet) {
        Set<String> r = new HashSet<String>();
        r.addAll(ruleSet);
        //String r = rule; // = rule == null ? "":rule;
        if (rangeAttrMap.containsKey(id)) {
            //r += "+" + rangeAttrMap.get(id);
            r.addAll(rangeAttrMap.get(id));
        }
        return r;//rule == null ? "":rule;
    }
    
    /**
     * Check and Save Rule
     *
     * @param id concept id
     * @param rule rule
     */
    private void checkAndSaveRule(String id, Set<String> ruleSet) {
        Set<String> rSet = new HashSet<String>();
        if (ruleSet.size() > 0) {
            if (!conceptRuleMap.containsKey(id)) {
                rSet.addAll(ruleSet);
                conceptRuleMap.put(id, rSet);
            } else {
                rSet = conceptRuleMap.get(id);
                rSet.addAll(ruleSet);
            }
        }
    }
    
    /**
     * Concat parent path and path
     *
     * @param
     * return path
     */
    private String concatPath(String ancestorPath, String path) {
        return ancestorPath + "~" + path;
    }
    
    /**
     * Compute paths.
     *
     * @param par the parent
     * @param pPath the parent path
     * @param parChd children by parent
     * @param conceptPathMap paths by conceptId
     */
    private void travelSubtypes(String par, Set<String> chd, Map<String, Set<String>> parChd, Set<String> ruleSet) {
        Set<String> c = null;
        Set<String> rs;
        for (String code : chd) {
            
            // if Range59가지 중에 걸린다면 Map<ConceptId, AttrIds>
            //
            // symbol type은 나중에 검색할때 처리 지금안해줘도된다.
            // 검색할때 선택한 Attribute type과 검색어를 날리면 서버에서 해당속성의 각 Range별 Symbol을 찾아와서 조건별 검색을 한다.
            // <<라면 별도처리안함, <라면 제외할컨셉을 조건으로 건다, <=라면 제외할 슈퍼카테고리 그룹을 조건으로 건다., == 는 대상을 해당컨셉으로 제한한다.
            // rule += '+' + ATTRIDS
            rs = new HashSet<String>();
            rs.addAll(ruleSet);
            rs = appendRule(code, rs);
            checkAndSaveRule(code, rs);
            
            if (parChd.containsKey(code)) {
                c = parChd.get(code);
                travelSubtypes(code, c, parChd, rs);
            }
            lineCount++;
        }
    }
    
    /**
     * Compute MRCM Map
     *
     * 주어진 객체 : MRCM Rule Buffer from Table
     * 반환  객체 : Map<ValueId:String, AttrId:String>
     */
    public void makeRangeAttrMap() throws Exception {
        String attrId = ""; String valId = ""; Set<String> ruleSet; String line;
        rangeAttrMap = new HashMap<String, Set<String>>();
        
        File mf = new File(mrcmFile);
        if (!mf.exists()) {
            throw new Exception("오류 메시지: Mrcm 파일이 존재하지 않습니다.");
        }
        
        @SuppressWarnings("resource")
        BufferedReader in = new BufferedReader(new FileReader(mf));
        
        while ((line = in.readLine()) != null) {
            String[] tokens = line.split(lineChar);
            
            // Attribute Id
            attrId = tokens[0];
            // Value Id
            valId = tokens[2];
            
            if (rangeAttrMap.containsKey(valId)) {
                ruleSet = rangeAttrMap.get(valId);
            } else {
                ruleSet = new HashSet<String>();
                rangeAttrMap.put(valId, ruleSet);
            }
            ruleSet.add(attrId);
        }
        
        in.close();
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
            MrcmAppender generator = new MrcmAppender();
            generator.setRelationshipsFile(argv[0]);
            generator.setIndexFile(argv[1]);
            generator.setMrcmFile(argv[2]);
            generator.setOutputFile(argv[3]);
            generator.makeRangeAttrMap();
            generator.compute();
            
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
    
}

