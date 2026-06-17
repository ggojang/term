package co.infoclinic.term.hira.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import co.infoclinic.term.hira.service.HiraService;

@RestController
@RequestMapping("/hira")
public class HiraController {

    @Autowired
    private HiraService svc;

    // ─── 행위 ────────────────────────────────────────────────────────────────
    @RequestMapping(value = "/행위/tree", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeRoot() {
        return svc.get행위TreeRoot();
    }

    @RequestMapping(value = "/행위/tree/{sheet}", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeSheet(@PathVariable String sheet) {
        return svc.get행위TreeBySheet(sheet);
    }

    @RequestMapping(value = "/행위/tree/{sheet}/{jang}", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeJang(@PathVariable String sheet,
                                                  @PathVariable String jang) {
        return svc.get행위TreeByJang(sheet, jang);
    }

    @RequestMapping(value = "/행위/tree/{sheet}/{jang}/{jeol}", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeJeol(@PathVariable String sheet,
                                                  @PathVariable String jang,
                                                  @PathVariable String jeol) {
        return svc.get행위TreeByJeol(sheet, jang, jeol);
    }

    @RequestMapping(value = "/행위/tree/{sheet}/{jang}/{jeol}/{sedo}", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeSedo(@PathVariable String sheet,
                                                  @PathVariable String jang,
                                                  @PathVariable String jeol,
                                                  @PathVariable String sedo) {
        return svc.get행위TreeBySedo(sheet, jang, jeol, sedo);
    }

    @RequestMapping(value = "/행위/tree/{sheet}/{jang}/{jeol}/{sedo}/{classNo:.+}", method = RequestMethod.GET)
    public List<Map<String, Object>> 행위TreeClassNo(@PathVariable String sheet,
                                                     @PathVariable String jang,
                                                     @PathVariable String jeol,
                                                     @PathVariable String sedo,
                                                     @PathVariable String classNo) {
        return svc.get행위TreeByClassNo(sheet, jang, jeol, sedo, classNo);
    }

    @RequestMapping(value = "/행위/{code:.+}", method = RequestMethod.GET)
    public Map<String, Object> 행위Detail(@PathVariable String code) {
        return svc.get행위Detail(code);
    }

    @RequestMapping(value = "/행위/search", method = RequestMethod.GET)
    public Map<String, Object> 행위Search(@RequestParam String q,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        return svc.search행위(q, page, size);
    }

    // ─── 약제 ATC 트리 ───────────────────────────────────────────────────────
    @RequestMapping(value = "/약제/atc/tree", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제ATCRoot() {
        return svc.get약제ATCRoot();
    }

    @RequestMapping(value = "/약제/atc/tree/{code:.+}", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제ATCChildren(@PathVariable String code) {
        return svc.get약제ATCChildren(code);
    }

    @RequestMapping(value = "/약제/atc/{code:.+}", method = RequestMethod.GET)
    public Map<String, Object> 약제ATCDetail(@PathVariable String code) {
        return svc.get약제ATCDetail(code);
    }

    @RequestMapping(value = "/약제/atc/search", method = RequestMethod.GET)
    public Map<String, Object> 약제ATCSearch(@RequestParam String q,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "100") int size) {
        return svc.search약제ATC(q, page, size);
    }

    // ─── 약제 ────────────────────────────────────────────────────────────────
    @RequestMapping(value = "/약제/tree", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제TreeRoot() {
        return svc.get약제TreeRoot();
    }

    @RequestMapping(value = "/약제/tree/{ingName}", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제TreeByIng(@PathVariable String ingName) {
        return svc.get약제TreeByDiv(ingName);
    }

    @RequestMapping(value = "/약제/tree/{ingName}/{productBase:.+}", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제TreeByProduct(@PathVariable String ingName,
                                                       @PathVariable String productBase) {
        return svc.get약제TreeByProduct(ingName, productBase);
    }

    @RequestMapping(value = "/약제/{code:.+}", method = RequestMethod.GET)
    public Map<String, Object> 약제Detail(@PathVariable String code) {
        return svc.get약제Detail(code);
    }

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

    @RequestMapping(value = "/치료재료/tree/{major}", method = RequestMethod.GET)
    public List<Map<String, Object>> 치료재료TreeMajor(@PathVariable String major) {
        return svc.get치료재료TreeByMajor(major);
    }

    @RequestMapping(value = "/치료재료/tree/{major}/{midCode}", method = RequestMethod.GET)
    public List<Map<String, Object>> 치료재료TreeMid(@PathVariable String major,
                                                    @PathVariable String midCode) {
        return svc.get치료재료TreeByMid(midCode);
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
