package co.infoclinic.term.icd10.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QIcd10Class is a Querydsl query type for Icd10Class
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QIcd10Class extends EntityPathBase<Icd10Class> {

    private static final long serialVersionUID = 232419684L;

    public static final QIcd10Class icd10Class = new QIcd10Class("icd10Class");

    public final NumberPath<Integer> childrenCount = createNumber("childrenCount", Integer.class);

    public final StringPath classKind = createString("classKind");

    public final StringPath code = createString("code");

    public final NumberPath<Integer> descendantCount = createNumber("descendantCount", Integer.class);

    public final ListPath<Icd10Rubric, QIcd10Rubric> icd10Rubric = this.<Icd10Rubric, QIcd10Rubric>createList("icd10Rubric", Icd10Rubric.class, QIcd10Rubric.class, PathInits.DIRECT2);

    public final StringPath label = createString("label");

    public final StringPath path = createString("path");

    public final StringPath ref = createString("ref");

    public final StringPath superClass = createString("superClass");

    public final StringPath usageKind = createString("usageKind");

    public final StringPath version = createString("version");

    public QIcd10Class(String variable) {
        super(Icd10Class.class, forVariable(variable));
    }

    public QIcd10Class(Path<? extends Icd10Class> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIcd10Class(PathMetadata<?> metadata) {
        super(Icd10Class.class, metadata);
    }

}

