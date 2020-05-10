metadata {
	definition (name: "eWeLink Connector Switch", namespace: "ewelinkconnector", author: "theseesun", runLocally: false, mnmn: "eWeLink", vid: "generic-switch") {
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
        capability "Health Check"
        capability "Sensor"
        
        command "onPhysical"
        command "offPhysical"
        
        command  "markDeviceOnline"
        command  "markDeviceOffline"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
        }
        standardTile("on", "device.switch", decoration: "flat") {
            state "default", label: 'On', action: "onPhysical", backgroundColor: "#ffffff"
        }
        standardTile("off", "device.switch", decoration: "flat") {
            state "default", label: 'Off', action: "offPhysical", backgroundColor: "#ffffff"
        }
     	standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

       standardTile("deviceHealthControl", "device.healthStatus", decoration: "flat", width: 1, height: 1, inactiveLabel: false) {
            state "online",  label: "ONLINE", backgroundColor: "#00A0DC", action: "markDeviceOffline", icon: "st.Health & Wellness.health9", nextState: "goingOffline", defaultState: true
            state "offline", label: "OFFLINE", backgroundColor: "#E86D13", action: "markDeviceOnline", icon: "st.Health & Wellness.health9", nextState: "goingOnline"
            state "goingOnline", label: "Going ONLINE", backgroundColor: "#FFFFFF", icon: "st.Health & Wellness.health9"
            state "goingOffline", label: "Going OFFLINE", backgroundColor: "#FFFFFF", icon: "st.Health & Wellness.health9"
        }
        main "switch"
        details(["switch","on","off","refresh","deviceHealthControl"])
    }
    
    preferences {
    
    	section("Device") {
			input(name: "deviceid", type: "string", title: "Device ID", description: "eWeLink Device ID", displayDuringSetup: true, required: true)
		}
        
		section("Connector Host") {
			input(name: "url", type: "string", title: "URL", description: "URL of eWeLink Connector", displayDuringSetup: true, required: true, defaultValue: "http://")
			input(name: "port", type: "number", title: "Port", description: "Port", displayDuringSetup: true, required: true, defaultValue: 8700)
		}

		section("Authentication") {
			input(name: "email", type: "string", title: "E-mail", description: "eWeLink E-mail", displayDuringSetup: true, required: true)
			input(name: "password", type: "password", title: "Password", description: "eWeLink Password", displayDuringSetup: true, required: true)
            input(name: "region", type: "string", title: "Region", description: "eWeLink Region", displayDuringSetup: true, required: true, defaultValue: "us")            
		}
	}
    
}

def markDeviceOnline() {
    setDeviceHealth("online")
}

def markDeviceOffline() {
    sendEvent(name: "switch", value: "off")
    setDeviceHealth("offline")
}

private setDeviceHealth(String healthState) {
    log.debug("healthStatus: ${device.currentValue('healthStatus')}; DeviceWatch-DeviceStatus: ${device.currentValue('DeviceWatch-DeviceStatus')}")
    // ensure healthState is valid
    List validHealthStates = ["online", "offline"]
    healthState = validHealthStates.contains(healthState) ? healthState : device.currentValue("healthStatus")
    // set the healthState
    sendEvent(name: "DeviceWatch-DeviceStatus", value: healthState)
    sendEvent(name: "healthStatus", value: healthState)
}

def update(groovy.json.internal.LazyMap resultJson) {
	log.debug "resultJson:${resultJson}"
    
	if (resultJson?.result in ["SUCCESS"]) {
		if(resultJson?.status in ["ok"]){
        	markDeviceOnline()
        	if(resultJson?.state in ["on"]){
         		sendEvent(name: "switch", value:"on")
                 log.debug "on"
            }else{
            	sendEvent(name: "switch", value:"off")
                 log.debug "off"
            }
            sendEvent(name: "deviceHealthControl", value:"online")    
        }else{
         		markDeviceOffline()
        }
	}
	else{
		markDeviceOffline()
	}
}

def on() {
	log.debug "on"
    sendCommand("on")
}

def off() {
	log.debug "off"
	sendCommand("off")
}

def push() {
	log.debug "PUSH"
	sendCommand("toggle")
}

def poll() {
	log.debug "POLL"
	sendCommand("state")
}

def refresh() {
	log.debug "REFRESH"
	sendCommand("state")
}

def installed() {
  	log.trace "Executing 'installed'"
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
    markDeviceOffline()
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
	sendCommand("state")
}

private def sendCommand(String command) {
	log.debug "sendCommand(${command}) to device at $url:$port"
    if (!url || !port) {
     	setDeviceHealth("offline")
    	return
    }
	def path = "${url}:${port}/${command}/${deviceid}"
    
	def params = "email=${email}&password=${password}&region=${region}"
    
    log.debug "path:{$path} params:{$params}"
    
    try {
        httpPost(path, params) { resp ->
            log.debug "response data: ${resp.data}"
            log.debug "response contentType: ${resp.contentType}"
            update(resp.data)
        }
    } catch (e) {
        log.debug "something went wrong: $e"
    }
}

def onPhysical() {
    log.debug "$version onPhysical()"
    sendEvent(name: "switch", value: "on", type: "physical")
}

def offPhysical() {
    log.debug "$version offPhysical()"
    sendEvent(name: "switch", value: "off", type: "physical")
}