package ibm.gse.eda.mpoc.domain.util;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jb.kstreams.play.domain.Purchase;


@SuppressWarnings("DefaultAnnotationParam")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_t")
@JsonSubTypes({
                 @JsonSubTypes.Type(value = Purchase.class, name = "purchase")
             })
public interface JSONSerdeCompatible {

}
