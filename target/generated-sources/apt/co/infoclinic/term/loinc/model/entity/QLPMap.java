package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLPMap is a Querydsl query type for LPMap
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLPMap extends EntityPathBase<LPMap> {

    private static final long serialVersionUID = -947488495L;

    public static final QLPMap lPMap = new QLPMap("lPMap");

    public final StringPath contentOrigin = createString("contentOrigin");

    public final StringPath equivalence = createString("equivalence");

    public final StringPath extCodeDisplayName = createString("extCodeDisplayName");

    public final StringPath extCodeId = createString("extCodeId");

    public final StringPath extCodeSystem = createString("extCodeSystem");

    public final StringPath extCodeSystemCopyrightNotice = createString("extCodeSystemCopyrightNotice");

    public final StringPath extCodeSystemVersion = createString("extCodeSystemVersion");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath partName = createString("partName");

    public final StringPath partNumber = createString("partNumber");

    public final StringPath partTypeName = createString("partTypeName");

    public QLPMap(String variable) {
        super(LPMap.class, forVariable(variable));
    }

    public QLPMap(Path<? extends LPMap> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLPMap(PathMetadata<?> metadata) {
        super(LPMap.class, metadata);
    }

}

