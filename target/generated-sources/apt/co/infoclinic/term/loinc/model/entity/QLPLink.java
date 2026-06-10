package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLPLink is a Querydsl query type for LPLink
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLPLink extends EntityPathBase<LPLink> {

    private static final long serialVersionUID = 692605669L;

    public static final QLPLink lPLink = new QLPLink("lPLink");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath linkTypeName = createString("linkTypeName");

    public final StringPath loincNumber = createString("loincNumber");

    public final StringPath longCommonName = createString("longCommonName");

    public final StringPath partCodeSystem = createString("partCodeSystem");

    public final StringPath partName = createString("partName");

    public final StringPath partNumber = createString("partNumber");

    public final StringPath partTypeName = createString("partTypeName");

    public final StringPath property = createString("property");

    public QLPLink(String variable) {
        super(LPLink.class, forVariable(variable));
    }

    public QLPLink(Path<? extends LPLink> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLPLink(PathMetadata<?> metadata) {
        super(LPLink.class, metadata);
    }

}

