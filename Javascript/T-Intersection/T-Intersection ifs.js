function priority() {
		var carW = cars.splice(0, 1);
		var carE = cars.splice(0, 1);
		var carS = cars.splice(0, 1);
		
		if(carW != null) {
			if(carW.indicator == 0) {
				if(carS != null) {
					if(carS.indicator == 1) {
						//stop
					}else if(carS.indicator == -1) {
						//stop
					}
				}else if(carS == null) {
					this.send('OEAST',carW);
				}
			}else if(carW.indicator == 1) {
				if(carS != null) {
					if(carS.indicator == 1) {
						this.send('OSOUTH',carW);
					}else if(carS.indicator == -1) {
						this.send('OSOUTH',carW);
					}
				}else if(carS == null) {
					this.send('OSouth',carW);
				}
			}
		}
		
		if(carE != null) {
			if(carE.indicator == 0) {
				this.send('OWEST',carE);
			}else if(carE.Indicator == -1) {
				if(carW != null) {
					if(carW.indicator == 0) {
						//stop
					}else if(carW.indicator == 1) {
						//stop
					}
				}else if(carW == null) {
					this.send('OWEST',carE);
				}
			}
		}
		
		if(carS != null) {
			if(carS.indicator == 1) {
				this.send('OEAST',carS);
			}else if(carS.indicator == -1) {
				if(carE != null) {
					//stop
				}else if(carE == null) {
					this.send('OWEST',carS);
				}
			}
		}