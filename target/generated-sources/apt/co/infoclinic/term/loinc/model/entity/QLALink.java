package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLALink is a Querydsl query type for LALink
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLALink extends EntityPathBase<LALink> {

    private static final long serialVersionUID = 678752854L;

    public static final QLALink lALink = new QLALink("lALink");

    public final StringPath applicableContext = createString("applicableContext");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath LAID = createString("LAID");

    public final StringPath LALinkType = createString("LALinkType");

    public final StringPath LAName = createString("LAName");

    public final StringPath loincNumber = createString("loincNumber");

    public final StringPath longCommonName = createString("longCommonName");

    public QLALink(String variable) {
        super(LALink.class, forVariable(variable));
    }

    public QLALink(Path<? extends LALink> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLALink(PathMetadata<?> metadata) {
        super(LALink.class, metadata);
    }

}

