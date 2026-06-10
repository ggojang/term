package co.infoclinic.term.icd10.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QIcd10Rubric is a Querydsl query type for Icd10Rubric
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QIcd10Rubric extends EntityPathBase<Icd10Rubric> {

    private static final long serialVersionUID = -947146815L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIcd10Rubric icd10Rubric = new QIcd10Rubric("icd10Rubric");

    public final StringPath code = createString("code");

    public final StringPath fragmentType = createString("fragmentType");

    public final QIcd10Class icd10Class;

    public final StringPath id = createString("id");

    public final StringPath kind = createString("kind");

    public final StringPath label = createString("label");

    public final StringPath lang = createString("lang");

    public final StringPath modifierCode = createString("modifierCode");

    public final StringPath paraType = createString("paraType");

    public final StringPath ref = createString("ref");

    public final NumberPath<Integer> seq = createNumber("seq", Integer.class);

    public final StringPath usageKind = createString("usageKind");

    public final StringPath version = createString("version");

    public QIcd10Rubric(String variable) {
        this(Icd10Rubric.class, forVariable(variable), INITS);
    }

    public QIcd10Rubric(Path<? extends Icd10Rubric> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QIcd10Rubric(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QIcd10Rubric(PathMetadata<?> metadata, PathInits inits) {
        this(Icd10Rubric.class, metadata, inits);
    }

    public QIcd10Rubric(Class<? extends Icd10Rubric> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.icd10Class = inits.isInitialized("icd10Class") ? new QIcd10Class(forProperty("icd10Class")) : null;
    }

}

