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

    // ─── 약제 ────────────────────────────────────────────────────────────────
    @RequestMapping(value = "/약제/tree", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제TreeRoot() {
        return svc.get약제TreeRoot();
    }

    @RequestMapping(value = "/약제/tree/{divCode}", method = RequestMethod.GET)
    public List<Map<String, Object>> 약제TreeDiv(@PathVariable String divCode) {
        return svc.get약제TreeByDiv(divCode);
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
