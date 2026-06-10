package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLinguisticVariant is a Querydsl query type for LinguisticVariant
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLinguisticVariant extends EntityPathBase<LinguisticVariant> {

    private static final long serialVersionUID = 754027449L;

    public static final QLinguisticVariant linguisticVariant = new QLinguisticVariant("linguisticVariant");

    public final StringPath className = createString("className");

    public final StringPath code = createString("code");

    public final StringPath component = createString("component");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath isoCountry = createString("isoCountry");

    public final StringPath isoLang = createString("isoLang");

    public final StringPath lang = createString("lang");

    public final StringPath longCommonName = createString("longCommonName");

    public final StringPath methodType = createString("methodType");

    public final StringPath property = createString("property");

    public final StringPath relatedNames2 = createString("relatedNames2");

    public final StringPath scaleType = createString("scaleType");

    public final StringPath shortName = createString("shortName");

    public final StringPath system = createString("system");

    public final StringPath timeAspect = createString("timeAspect");

    public QLinguisticVariant(String variable) {
        super(LinguisticVariant.class, forVariable(variable));
    }

    public QLinguisticVariant(Path<? extends LinguisticVariant> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLinguisticVariant(PathMetadata<?> metadata) {
        super(LinguisticVariant.class, metadata);
    }

}

