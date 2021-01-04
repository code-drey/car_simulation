
exports.setup = function () {
	//the Input-ports for the incoming cars
    this.input('carWEST');
    this.input('carEAST');
    this.input('carSOUTH');
    //the Output-ports for the leaving cars
    this.output('OWEST');
    this.output('OEAST');
    this.output('OSOUTH');
}

exports.initialize = function () {
	//creates an Array that contains the cars that stand in an Intersection
	//following Array positions are relevant: 0 = WEST, 1 = EAST and 2 = SOUTH
	var QueueW = [];
	var QueueE = [];
	var QueueS = [];
	
	this.addInputHandler('carWEST', function() {
	QueueW.unshift(this.get('carWEST'));
	});
	
	this.addInputHandler('carEAST', function() {
	QueueE.unshift(this.get('carEAST'));
	});
	
	this.addInputHandler('carSOUTH', function() {
	QueueS.unshift(this.get('carSOUTH'));
	});


		if(QueueW.length != 0) {
			if(QueueW[QueueW.length - 1].indicator == 0) {
				if(QueueS.length != 0) {
					if(QueueS[QueueS.length - 1].indicator == 1) {
						//stop
						QueueW.pop();
						QueueW.push(QueueW[QueueW.length - 1]);
					}else if(QueueS[QueueS.length - 1].indicator == -1) {
						//stop
						QueueW.pop();
						QueueW.push(QueueW[QueueW.length - 1]);
					}
				}else if(QueueS.length == 0) {
					this.send('OEAST',QueueW[0]);
				}
			}else if(QueueW[QueueW.length - 1].indicator == 1) {
				if(QueueS.length != 0) {
					if(QueueS[QueueS.length - 1].indicator == 1) {
						this.send('OSOUTH',QueueW[0]);
					}else if(QueueS[QueueS.length - 1].indicator == -1) {
						this.send('OSOUTH',QueueW[0]);
					}
				}else if(QueueS.length == 0) {
					this.send('OSOUTH',QueueW[0]);
				}
			}
		}
		
		if(QueueE.length != 0) {
			if(QueueE[QueueE.length - 1].indicator == 0) {
				this.send('OWEST',QueueE[0]);
			}else if(QueueE[QueueE.length - 1].Indicator == -1) {
				if(QueueW.length != 0) {
					if(QueueW[QueueW.length - 1].indicator == 0) {
						//stop
						QueueE.pop();
						QueueE.push(QueueE[QueueE.length - 1]);
					}else if(QueueW[QueueW.length - 1].indicator == 1) {
						//stop
						QueueE.pop();
						QueueE.push(QueueE[QueueE.length - 1]);
					}
				}else if(QueueW.length == 0) {
					this.send('OWEST',QueueE[0]);
				}
			}
		}
		
		if(QueueS.length != 0) {
			if(QueueS[QueueS.length - 1].indicator == 1) {
				this.send('OEAST',QueueS[0]);
			}else if(QueueS[QueueS.length - 1].indicator == -1) {
				if(QueueE.length != 0) {
					//stop
					QueueS.pop();
					QueueS.push(QueueS[QueueS.length - 1]);
				}else if(QueueE.length == 0) {
					this.send('OWEST',QueueS[0]);
				}
			}
		}