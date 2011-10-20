if (typeof(com)=="undefined") {com = {};}
if (typeof(com.climbwithyourfeet)=="undefined") {com.climbwithyourfeet = {};}
if (typeof(com.climbwithyourfeet.proto)=="undefined") {com.climbwithyourfeet.proto = {};}

com.climbwithyourfeet.proto.ExampleMsg = PROTO.Message("com.climbwithyourfeet.proto.ExampleMsg",{
	name: {
		options: {},
		multiplicity: PROTO.required,
		type: function(){return PROTO.string;},
		id: 1
	},
	value: {
		options: {},
		multiplicity: PROTO.required,
		type: function(){return PROTO.string;},
		id: 2
	},
	code: {
		options: {},
		multiplicity: PROTO.required,
		type: function(){return PROTO.int32;},
		id: 3
	}});
com.climbwithyourfeet.proto.ExampleMessages = PROTO.Message("com.climbwithyourfeet.proto.ExampleMessages",{
	msg: {
		options: {},
		multiplicity: PROTO.repeated,
		type: function(){return com.climbwithyourfeet.proto.ExampleMsg;},
		id: 1
	}});
