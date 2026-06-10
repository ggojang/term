package co.infoclinic.term.icd10.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QIcd10Sibling is a Querydsl query type for Icd10Sibling
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QIcd10Sibling extends EntityPathBase<Icd10Sibling> {

    private static final long serialVersionUID = 1246995374L;

    public static final QIcd10Sibling icd10Sibling = new QIcd10Sibling("icd10Sibling");

    public final StringPath code = createString("code");

    public final StringPath fragmentType = createString("fragmentType");

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

    public QIcd10Sibling(String variable) {
        super(Icd10Sibling.class, forVariable(variable));
    }

    public QIcd10Sibling(Path<? extends Icd10Sibling> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIcd10Sibling(PathMetadata<?> metadata) {
        super(Icd10Sibling.class, metadata);
    }

}

