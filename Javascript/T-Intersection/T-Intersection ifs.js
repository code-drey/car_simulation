
exports.setup = function () {
	//the Input-ports for the incoming cars
    this.input('carWEST');
    this.input('carEAST');
    this.input('carSOUTH');
    //the Output-ports for the leaving cars
    this.output('OWEST');
    this.output('OEAST');
    this.output('OSOUTH');
    //the Output-ports for cars that have to wait
    this.output('waitW');
    this.output('waitE');
    this.output('waitS');
}

exports.initialize = function () {
	//creates an Array that contains the cars that stand in an Intersection
	//following Array positions are relevant: 0 = WEST, 1 = EAST and 2 = SOUTH
	var QueueW = [];
	var QueueE = [];
	var QueueS = [];
	
	this.addInputHandler('carWEST', function() {
	QueueW.add(this.get('carWEST'));
	});
	
	this.addInputHandler('carEAST', function() {
	QueueE.add(this.get('carEast'));
	});
	
	this.addInputHandler('carSOUTH', function() {
	QueueS.add(this.get('carSOUTH'));
	});

	while (QueueW.length() != 0 && QueueE.length() != 0 && QueueS.length() != 0) {
		if(carW != null) {
			if(carW.indicator == 0) {
				if(carS != null) {
					if(carS.indicator == 1) {
						//stop
						QueueW.add(carW);
					}else if(carS.indicator == -1) {
						//stop
						QueueW.add(carW);
					}
				}else if(carS == null) {
					this.send('OEAST',QueueW[0]);
				}
			}else if(carW.indicator == 1) {
				if(carS != null) {
					if(carS.indicator == 1) {
						this.send('OSOUTH',QueueW[0]);
					}else if(carS.indicator == -1) {
						this.send('OSOUTH',QueueW[0]);
					}
				}else if(carS == null) {
					this.send('OSOUTH',QueueW[0]);
				}
			}
		}
		
		if(carE != null) {
			if(carE.indicator == 0) {
				this.send('OWEST',QueueE[0]);
			}else if(carE.Indicator == -1) {
				if(carW != null) {
					if(carW.indicator == 0) {
						//stop
						QueueE.add(carE);
					}else if(carW.indicator == 1) {
						//stop
						QueueE.add(carE);
					}
				}else if(carW == null) {
					this.send('OWEST',QueueE[0]);
				}
			}
		}
		
		if(carS != null) {
			if(carS.indicator == 1) {
				this.send('OEAST',carS);
			}else if(carS.indicator == -1) {
				if(carE != null) {
					//stop
					QueueS.add(carS);
				}else if(carE == null) {
					this.send('OWEST',carS);
				}
			}
		}
	}