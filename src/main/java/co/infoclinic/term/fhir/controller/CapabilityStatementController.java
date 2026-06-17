package co.infoclinic.term.fhir.controller;

import ca.uhn.fhir.context.FhirContext;
import co.infoclinic.term.fhir.api.FhirApi;
import org.hl7.fhir.r4.model.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CapabilityStatementController {

    @RequestMapping(value = {FhirApi.BASE, FhirApi.BASE + "/"}, method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    public String fhirRoot() {
        return "<html><body style='font-family:sans-serif;padding:2em'>"
             + "<h2>STOM Browser FHIR Endpoint</h2>"
             + "<p>FHIR R4 Terminology Server</p>"
             + "<ul>"
             + "<li><a href='fhir/metadata'>CapabilityStatement</a></li>"
             + "<li>CodeSystem/$lookup, $validate-code, $subsumes</li>"
             + "<li>ValueSet/$expand, $validate-code</li>"
             + "<li>ConceptMap/$translate</li>"
             + "</ul>"
             + "<p style='color:#888'>Supported systems: SNOMED CT, LOINC, KCD-9, HIRA EDI (행위/약제/치료재료)</p>"
             + "</body></html>";
    }

    private static final FhirContext FHIR_CTX = FhirContext.forR4();

    @RequestMapping(value = FhirApi.METADATA, method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/fhir+json"})
    public String getCapabilityStatement(HttpServletRequest request) {
        String base = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/fhir";

        CapabilityStatement cs = new CapabilityStatement();
        cs.setId("terminology-server");
        cs.setUrl(base + "/metadata");
        cs.setVersion("1.0.0");
        cs.setName("STOMTerminologyServer");
        cs.setTitle("STOM FHIR Terminology Server");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setExperimental(false);
        cs.setPublisher("Infoclinic");
        cs.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);
        cs.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
        cs.addFormat("application/fhir+json");
        cs.addFormat("application/json");

        // Software
        CapabilityStatement.CapabilityStatementSoftwareComponent sw = new CapabilityStatement.CapabilityStatementSoftwareComponent();
        sw.setName("STOM Terminology Server");
        sw.setVersion("1.0.0");
        cs.setSoftware(sw);

        // Implementation
        CapabilityStatement.CapabilityStatementImplementationComponent impl = new CapabilityStatement.CapabilityStatementImplementationComponent();
        impl.setDescription("STOM FHIR R4 Terminology Server — SNOMED CT, LOINC, KCD-9 기반");
        impl.setUrl(base);
        cs.setImplementation(impl);

        // REST
        CapabilityStatement.CapabilityStatementRestComponent rest = new CapabilityStatement.CapabilityStatementRestComponent();
        rest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);

        // CodeSystem
        rest.addResource(buildResource("CodeSystem",
                new String[]{"read", "create", "update", "delete", "search-type"},
                new String[]{"url", "name", "status", "version"},
                new String[]{"$lookup", "$validate-code", "$subsumes"}));

        // ValueSet
        rest.addResource(buildResource("ValueSet",
                new String[]{"read", "create", "update", "delete", "search-type"},
                new String[]{"url", "name", "status", "version"},
                new String[]{"$expand", "$validate-code"}));

        // ConceptMap
        rest.addResource(buildResource("ConceptMap",
                new String[]{"read", "create", "update", "delete", "search-type"},
                new String[]{"url", "name", "status", "version"},
                new String[]{"$translate"}));

        // System operations
        rest.addOperation(buildOperation("$install-package", base + "/OperationDefinition/install-package"));

        cs.addRest(rest);

        return FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
    }

    private CapabilityStatement.CapabilityStatementRestResourceComponent buildResource(
            String type, String[] interactions, String[] searchParams, String[] operations) {

        CapabilityStatement.CapabilityStatementRestResourceComponent res =
                new CapabilityStatement.CapabilityStatementRestResourceComponent();
        res.setType(type);
        res.setVersioning(CapabilityStatement.ResourceVersionPolicy.NOVERSION);

        for (String i : interactions) {
            CapabilityStatement.ResourceInteractionComponent ic = new CapabilityStatement.ResourceInteractionComponent();
            ic.setCode(CapabilityStatement.TypeRestfulInteraction.fromCode(i));
            res.addInteraction(ic);
        }

        for (String sp : searchParams) {
            CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent spc =
                    new CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent();
            spc.setName(sp);
            spc.setType(Enumerations.SearchParamType.STRING);
            res.addSearchParam(spc);
        }

        for (String op : operations) {
            CapabilityStatement.CapabilityStatementRestResourceOperationComponent opc =
                    new CapabilityStatement.CapabilityStatementRestResourceOperationComponent();
            opc.setName(op);
            opc.setDefinition("OperationDefinition/" + type + "-" + op.replace("$", ""));
            res.addOperation(opc);
        }

        return res;
    }

    private CapabilityStatement.CapabilityStatementRestResourceOperationComponent buildOperation(String name, String def) {
        CapabilityStatement.CapabilityStatementRestResourceOperationComponent op =
                new CapabilityStatement.CapabilityStatementRestResourceOperationComponent();
        op.setName(name);
        op.setDefinition(def);
        return op;
    }
}
