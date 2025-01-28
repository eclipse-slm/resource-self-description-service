package org.eclipse.slm.self_description_service.common.model

class SystemVariable(

    val key: String,

    val name: String,

    val valueSource: SystemVariableValueSource,

    val valuePath: String,

    ) {

    var value: Any? = null

}
