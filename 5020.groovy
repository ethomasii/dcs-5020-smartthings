/**
 *  D-Link 5020
 *
 *  Author: ethomasii@gmail.com
 *  Date: 2013-12-08
 */
 // for the UI

preferences {
    input("username", "text", title: "Username", description: "Your DCS-5020L username (usually admin)")
    input("password", "password", title: "Password", description: "Your DCS-5020L password")
    input("host", "text", title: "URL", description: "The URL of your camera")
    input("port", "text", title: "Port", description: "The port")
}

metadata {
	// Automatically generated. Make future change here.
	definition (name: "D-Link 5020", author: "ethomasii@gmail.com") {
		capability "Image Capture"
        capability "Switch"

		attribute "motion", "string"

		command "left"
		command "right"
		command "up"
		command "down"
		command "setMotion"
	}

	tiles {
		
        carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }
        
        standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "", action: "Image Capture.take", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
		}

		standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false, decoration: "flat") {
			state "take", label: "", action: "Image Capture.take", icon: "st.secondary.take", nextState:"taking"
		}
        
        standardTile("left", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
			state "take", label: "left", action: "left", icon: ""
		}
        
       	standardTile("up", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
			state "take", label: "up", action: "up", icon: ""
		}
        
        standardTile("right", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
			state "take", label: "right", action: "right", icon: ""
		}
        
        
        standardTile("down", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "down", label: "down", action: "down", icon: ""
		}

        standardTile("down", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "down", label: "down", action: "down", icon: ""
		}
        
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.custom.buttons.rec", backgroundColor: "#ccffcc"
			state "on", label: 'On', action: "switch.off", icon: "st.custom.buttons.rec", backgroundColor: "#EE0000"
		}
		main "button"
		details(["cameraDetails", "take","up","left","right","down","button"])
	}
}


def parseCameraResponse(def response) {
    if (response.headers.'Content-Type'.contains("image/jpeg")) {
        def imageBytes = response.data
        if (imageBytes) {
            storeImage(getPictureName(), imageBytes)
        }
    } 
}


private getPictureName() {
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
	"image" + "_$pictureUuid" + ".jpg"
}



private take() {
    log.debug "take a photo"

	httpGet("http://${settings.username}:${settings.password}@${settings.host}:${settings.port}/image/jpeg.cgi") {response ->        
        log.debug "image captured"
        parseCameraResponse(response)
    }
}

def ptzCmd(int num1, int num2, int num3)
{
  httpPost("http://${settings.username}:${settings.password}@${settings.host}:${settings.port}/pantiltcontrol.cgi", "PanSingleMoveDegree=${num1}&TiltSingleMoveDegree=${num2}&PanTiltSingleMove=${num3}") {response -> 
        def content = response.data
        log.debug content
    }
}

def motionCmd(int motion)
{
  sendEvent(name: 'motion', value: motion)
  httpPost("http://${settings.username}:${settings.password}@${settings.host}:${settings.port}/setSystemMotion", "ReplySuccessPage=motion.htm&ReplyErrorPage=motion.htm&MotionDetectionEnable=${motion}&MotionDetectionSensitivity=85&ConfigSystemMotion=Save") {response -> 
        def content = response.data
        log.debug content
    }
}

def sendCmd(int num)
{
	httpGet("http://${settings.username}:${settings.password}@${settings.host}:${settings.port}/decoder_control.cgi?command=${num}") {response -> 
        def content = response.data
        log.debug content
    }
}

def left() {
	log.debug "Executing 'left'"
//    sendCmd(6)
      ptzCmd(5,5,3)
}

def right() {
	log.debug "Executing 'right'"
    ptzCmd(5,5,5)
}

def up() {
	log.debug "Executing 'up'"
    ptzCmd(5,5,1)
}

def down() {
	log.debug "Executing 'down'"
    ptzCmd(5,5,7)
}

def on() {
	log.debug "Executing motion detection"
    motionCmd(1)
    sendEvent(name: "switch", value: "on")
    
}

def off() {
	log.debug "Disabling motion detection"
    motionCmd(0)
    sendEvent(name: "switch", value: "off")
}

 
def setMotion(status) {
	log.debug "Status: $status"
    motionCmd(status) {
        sendEvent(name: 'motion', value: status)
    }
}

