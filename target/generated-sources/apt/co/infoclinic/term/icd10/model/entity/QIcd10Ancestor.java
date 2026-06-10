package co.infoclinic.term.icd10.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QIcd10Ancestor is a Querydsl query type for Icd10Ancestor
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QIcd10Ancestor extends EntityPathBase<Icd10Ancestor> {

    private static final long serialVersionUID = -1138644377L;

    public static final QIcd10Ancestor icd10Ancestor = new QIcd10Ancestor("icd10Ancestor");

    public final NumberPath<Integer> childrenCount = createNumber("childrenCount", Integer.class);

    public final StringPath classKind = createString("classKind");

    public final StringPath code = createString("code");

    public final NumberPath<Integer> descendantCount = createNumber("descendantCount", Integer.class);

    public final StringPath label = createString("label");

    public final StringPath path = createString("path");

    public final StringPath ref = createString("ref");

    public final StringPath superClass = createString("superClass");

    public final StringPath usageKind = createString("usageKind");

    public final StringPath version = createString("version");

    public QIcd10Ancestor(String variable) {
        super(Icd10Ancestor.class, forVariable(variable));
    }

    public QIcd10Ancestor(Path<? extends Icd10Ancestor> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIcd10Ancestor(PathMetadata<?> metadata) {
        super(Icd10Ancestor.class, metadata);
    }

}

