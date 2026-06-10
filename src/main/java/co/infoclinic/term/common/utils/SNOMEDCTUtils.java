package co.infoclinic.term.common.utils;

import javax.persistence.Table;

public final class SNOMEDCTUtils {
  public static final String CodeSystem = "SNOMEDCT";
  public static final String Concept = "concept";
  public static final String Description = "description";
  public static final String Relationship = "relationship";

  public static final String CODE_PATTERN = "^[vV]\\d{8}$";
  
  private SNOMEDCTUtils() {} // Prevent the class from being constructed

  public static class MetadataType {
    public static String Referenceset = "900000000000455006";
    public static String Descriptor = "900000000000456007";
  }

  public static class DescriptionType {
    public static String FullySpecifiedName = "900000000000003001";
    public static String Synonym = "900000000000013009";
    public static String Definition = "900000000000550004";
  }

  public static class ReferenceSetType {
    public static String AttributeValue = "900000000000480006";
    public static String Language = "900000000000506000";
    public static String SimpleMap = "900000000000496009";
    public static String ComplexMap = "447250001";
  }

  public static class RelationshipType {
    // characteristic type
    public static String Inferred = "900000000000011006";
    public static String Stated = "900000000000010007";
  }

  public static class ComponentType {
    public static String ConceptTypeComponent = "900000000000461009";
    public static String DescriptionTypeComponent = "900000000000462002";
    public static String RelationshipTypeComponent = "900000000000463007";
    public static String ComponentType = "900000000000460005";
  }

  public static class DefinitionStatus {
    public static String Primitive = "900000000000074008";
    public static String Defined = "900000000000073002";

    public static String getName(String definitionStatusId) {
      if (definitionStatusId.equals(Primitive)) {
        return "Primitive";
      } else if (definitionStatusId.equals(Defined)) {
        return "Defined";
      }
      return null;
    }
  }

  public static class PrimaryId {
    public static String SnomedCTConcept = "138875005";
    public static String IsA = "116680003";
  }

  public static String getTableNameByEntity(Class<?> c) {
    Table table = c.getAnnotation(Table.class);
    return table.name();
  }
}
