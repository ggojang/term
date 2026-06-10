package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QMrcmConstraints is a Querydsl query type for MrcmConstraints
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QMrcmConstraints extends EntityPathBase<MrcmConstraints> {

    private static final long serialVersionUID = -823706246L;

    public static final QMrcmConstraints mrcmConstraints = new QMrcmConstraints("mrcmConstraints");

    public final StringPath attributeId = createString("attributeId");

    public final StringPath attributeName = createString("attributeName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath sourceId = createString("sourceId");

    public final StringPath sourceName = createString("sourceName");

    public final StringPath valueId = createString("valueId");

    public final StringPath valueName = createString("valueName");

    public QMrcmConstraints(String variable) {
        super(MrcmConstraints.class, forVariable(variable));
    }

    public QMrcmConstraints(Path<? extends MrcmConstraints> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMrcmConstraints(PathMetadata<?> metadata) {
        super(MrcmConstraints.class, metadata);
    }

}

