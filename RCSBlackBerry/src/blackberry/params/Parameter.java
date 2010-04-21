/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Parameter.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.params;

// TODO: Auto-generated Javadoc
/**
 * The Class Parameter.
 */
public class Parameter {

    /**
     * Factory.
     * 
     * @param paramsId
     *            the params id
     * @param confParams
     *            the conf params
     * @return the parameter
     */
    public static Parameter factory(int paramsId, byte[] confParams) {
        Parameter parameter = new Parameter(paramsId, confParams);
        return parameter;
    }

    /** The Parameter id. */
    public int parameterId = -1;

    /** The Conf params. */
    byte[] confParams;

    /**
     * Instantiates a new parameter.
     * 
     * @param paramsId
     *            the params id
     * @param confParams
     *            the conf params
     */
    public Parameter(int parameterId_, byte[] confParams_) {
        this.parameterId = parameterId_;
        this.confParams = confParams_;
    }
}
