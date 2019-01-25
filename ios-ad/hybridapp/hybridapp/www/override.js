SkyEngine.BGM = CLASS({
	
	init : (inner, self, params) => {
		//REQUIRED: params
		//OPTIONAL: params.ogg
		//OPTIONAL: params.mp3
		//OPTIONAL: params.volume
		//OPTIONAL: params.isAudioTagMode
		
		let ogg = params.ogg;
		let mp3 = params.mp3;
		let volume = params.volume;
		let isAudioTagMode = params.isAudioTagMode;
		
		if (isAudioTagMode !== true) {
			
			let sound = SOUND({
				ogg : ogg,
				mp3 : mp3,
				volume : volume,
				isLoop : true
			});
			
			// 다른 화면을 보는 중에는 배경 음악을 일시정지합니다.
			let visibilitychangeEvent = EVENT('visibilitychange', () => {
				if (document.hidden === true) {
					sound.pause();
				} else {
					sound.play();
				}
			});
			
			let play = self.play = () => {
				sound.play();
			};
			
			let pause = self.pause = () => {
				sound.pause();
			};
			
			let stop = self.stop = () => {
				
				if (visibilitychangeEvent !== undefined) {
					visibilitychangeEvent.remove();
					visibilitychangeEvent = undefined;
				}
				
				if (sound !== undefined) {
					sound.stop();
					sound = undefined;
				}
			};
			
			let setVolume = self.setVolume = (volume) => {
				//REQUIRED: volume
				
				sound.setVolume(volume);
			};
			
			let getVolume = self.getVolume = () => {
				return sound.getVolume();
			};
			
			let fadeIn = self.fadeIn = (seconds) => {
				//REQUIRED: seconds
				
				sound.fadeIn(seconds);
			};
			
			let fadeOut = self.fadeOut = (seconds) => {
				//REQUIRED: seconds
				
				if (visibilitychangeEvent !== undefined) {
					visibilitychangeEvent.remove();
					visibilitychangeEvent = undefined;
				}
				
				if (sound !== undefined) {
					sound.fadeOut(seconds);
					sound = undefined;
				}
			};
		}
		
		else {
			
			if (volume === undefined) {
				volume = 0.8;
			}
			
			// 다른 화면을 보는 중에는 배경 음악을 일시정지합니다.
			let visibilitychangeEvent = EVENT('visibilitychange', () => {
				if (document.hidden === true) {
					Native.pauseBGM(mp3);
				} else {
					Native.playBGM(mp3);
				}
			});
			
			let play = self.play = () => {
				Native.playBGM(mp3);
			};
			
			let pause = self.pause = () => {
				Native.pauseBGM(mp3);
			};
			
			let stop = self.stop = () => {
				
				if (visibilitychangeEvent !== undefined) {
					visibilitychangeEvent.remove();
					visibilitychangeEvent = undefined;
				}
				
				Native.stopBGM(mp3);
			};
			
			let setVolume = self.setVolume = (_volume) => {
				//REQUIRED: volume
				
				volume = _volume;
				
				Native.setBGMVolume({
					path : mp3,
					volume : volume
				});
			};
			
			setVolume(volume);
			
			let getVolume = self.getVolume = () => {
				return volume;
			};
			
			let fadeIn = self.fadeIn = (seconds) => {
				//REQUIRED: seconds
				
				play();
			};
			
			let fadeOut = self.fadeOut = (seconds) => {
				//REQUIRED: seconds
				
				stop();
			};
		}
	}
});
