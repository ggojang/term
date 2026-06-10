package co.infoclinic.term.icd10.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QIcd10Children is a Querydsl query type for Icd10Children
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QIcd10Children extends EntityPathBase<Icd10Children> {

    private static final long serialVersionUID = 1494711955L;

    public static final QIcd10Children icd10Children = new QIcd10Children("icd10Children");

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

    public QIcd10Children(String variable) {
        super(Icd10Children.class, forVariable(variable));
    }

    public QIcd10Children(Path<? extends Icd10Children> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIcd10Children(PathMetadata<?> metadata) {
        super(Icd10Children.class, metadata);
    }

}

