package co.infoclinic.term.hira.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import co.infoclinic.term.hira.service.HiraService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "IV-01. HIRA")
@RestController
@RequestMapping("/hira")
public class HiraController {

    @Autowired
    private HiraService svc;

    // ─── 행위 ────────────────────────────────────────────────────────────────
    @ApiOperation(value = "HIRA 행위 트리 조회 [GET]")
    @RequestMapping(value = "/행위/tree", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeRoot() {
        return svc.get행위TreeRoot();
    }

    @ApiOperation(value = "HIRA 행위 트리 조회 (하위) [GET]")
    @RequestMapping(value = "/행위/tree/{sheet}", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeSheet(@PathVariable String sheet) {
        return svc.get행위TreeBySheet(sheet);
    }

    @ApiOperation(value = "HIRA 행위 트리 조회 (하위) [GET]")
    @RequestMapping(value = "/행위/tree/{sheet}/{jang}", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeJang(@PathVariable String sheet,
                                                  @PathVariable String jang) {
        return svc.get행위TreeByJang(sheet, jang);
    }

    @ApiOperation(value = "HIRA 행위 트리 조회 (하위) [GET]")
    @RequestMapping(value = "/행위/tree/{sheet}/{jang}/{jeol}", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeJeol(@PathVariable String sheet,
                                                  @PathVariable String jang,
                                                  @PathVariable String jeol) {
        return svc.get행위TreeByJeol(sheet, jang, jeol);
    }

    @ApiOperation(value = "HIRA 행위 트리 조회 (하위) [GET]")
    @RequestMapping(value = "/행위/tree/{sheet}/{jang}/{jeol}/{sedo}", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeSedo(@PathVariable String sheet,
                                                  @PathVariable String jang,
                                                  @PathVariable String jeol,
                                                  @PathVariable String sedo) {
        return svc.get행위TreeBySedo(sheet, jang, jeol, sedo);
    }

    @ApiOperation(value = "HIRA 행위 트리 조회 (하위) [GET]")
    @RequestMapping(value = "/행위/tree/{sheet}/{jang}/{jeol}/{sedo}/{classNo:.+}", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeClassNo(@PathVariable String sheet,
                                                     @PathVariable String jang,
                                                     @PathVariable String jeol,
                                                     @PathVariable String sedo,
                                                     @PathVariable String classNo) {
        return svc.get행위TreeByClassNo(sheet, jang, jeol, sedo, classNo);
    }

    @ApiOperation(value = "HIRA 행위 코드 단건 조회 [GET]")
    @RequestMapping(value = "/행위/{code:.+}", method = RequestMethod.GET)
    public Map<String, Object> 행위Detail(@PathVariable String code) {
        return svc.get행위Detail(code);
    }

    @ApiOperation(value = "HIRA 행위 검색 [GET]")
    @RequestMapping(value = "/행위/search", method = RequestMethod.GET)
    public Map<String, Object> 행위Search(@RequestParam String q,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        return svc.search행위(q, page, size);
    }

    // ─── 약제 ATC 트리 ───────────────────────────────────────────────────────
    @ApiOperation(value = "HIRA 약제 조회 [GET]")
    @RequestMapping(value = "/약제/atc/tree", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제ATCRoot() {
        return svc.get약제ATCRoot();
    }

    @ApiOperation(value = "HIRA 약제 조회 [GET]")
    @RequestMapping(value = "/약제/atc/tree/{code:.+}", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제ATCChildren(@PathVariable String code) {
        return svc.get약제ATCChildren(code);
    }

    @ApiOperation(value = "HIRA 약제 조회 [GET]")
    @RequestMapping(value = "/약제/atc/{code:.+}", method = RequestMethod.GET)
    public Map<String, Object> 약제ATCDetail(@PathVariable String code) {
        return svc.get약제ATCDetail(code);
    }

    @ApiOperation(value = "HIRA 약제 조회 [GET]")
    @RequestMapping(value = "/약제/atc/search", method = RequestMethod.GET)
    public Map<String, Object> 약제ATCSearch(@RequestParam String q,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "100") int size) {
        return svc.search약제ATC(q, page, size);
    }

    // ─── 약제 ────────────────────────────────────────────────────────────────
    @ApiOperation(value = "HIRA 약제 조회 [GET]")
    @RequestMapping(value = "/약제/tree", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제TreeRoot() {
        return svc.get약제TreeRoot();
    }

    @ApiOperation(value = "HIRA 약제 조회 [GET]")
    @RequestMapping(value = "/약제/tree/{ingName}", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제TreeByIng(@PathVariable String ingName) {
        return svc.get약제TreeByDiv(ingName);
    }

    @ApiOperation(value = "HIRA 약제 조회 [GET]")
    @RequestMapping(value = "/약제/tree/{ingName}/{productBase:.+}", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제TreeByProduct(@PathVariable String ingName,
                                                       @PathVariable String productBase) {
        return svc.get약제TreeByProduct(ingName, productBase);
    }

    @ApiOperation(value = "HIRA 약제 조회 [GET]")
    @RequestMapping(value = "/약제/{code:.+}", method = RequestMethod.GET)
    public Map<String, Object> 약제Detail(@PathVariable String code) {
        return svc.get약제Detail(code);
    }

    @ApiOperation(value = "HIRA 약제 조회 [GET]")
    @RequestMapping(value = "/약제/search", method = RequestMethod.GET)
    public Map<String, Object> 약제Search(@RequestParam String q,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        return svc.search약제(q, page, size);
    }

    // ─── 치료재료 ─────────────────────────────────────────────────────────────
    @RequestMapping(value = "/치료재료/tree", method = RequestMethod.GET)
    public List<Map<String, Object>> 치료재료TreeRoot() {
        return svc.get치료재료TreeRoot();
    }

    @RequestMapping(value = "/치료재료/tree/{sheet:.+}", method = RequestMethod.GET)
    public List<Map<String, Object>> 치료재료TreeSheet(@PathVariable String sheet) {
        return svc.get치료재료TreeBySheet(sheet);
    }

    @RequestMapping(value = "/치료재료/tree/{sheet}/{midCode:.+}", method = RequestMethod.GET)
    public List<Map<String, Object>> 치료재료TreeMid(@PathVariable String sheet,
                                                    @PathVariable String midCode) {
        return svc.get치료재료TreeByMid(sheet, midCode);
    }

    @RequestMapping(value = "/치료재료/{code:.+}", method = RequestMethod.GET)
    public Map<String, Object> 치료재료Detail(@PathVariable String code) {
        return svc.get치료재료Detail(code);
    }

    @RequestMapping(value = "/치료재료/search", method = RequestMethod.GET)
    public Map<String, Object> 치료재료Search(@RequestParam String q,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "50") int size) {
        return svc.search치료재료(q, page, size);
    }
}
