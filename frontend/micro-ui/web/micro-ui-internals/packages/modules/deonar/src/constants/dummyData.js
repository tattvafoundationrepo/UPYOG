//added again
export const importTypeOptions = [
    {code: '1', name: 'IMPORT_TYPE_1'},
    {code: '2', name: 'IMPORT_TYPE_2'}
];

export const traderNameOptions = [
    {code: '1', name: 'TRADER_1'},
    {code: '2', name: 'TRADER_2'}
];

export const gawalNameOptions = [
    {code: '1', name: 'GAWAL_1'},
    {code: '2', name: 'GAWAL_2'}
];

export const brokerNameOptions = [
    {value: '1', name: 'BROKER_1'},
    {value: '2', name: 'BROKER_2'}
];

export const shopkeeperNameOptions = [
    {code: '1', name: 'SHOPKEEPER_1'},
    {code: '2', name: 'SHOPKEEPER_2'}
];

export const dawanwalaNameOptions = [
    {code: '1', name: 'DAWANWALA_1'},
    {code: '2', name: 'DAWANWALA_2'}
];

export const dairywalaNameOptions = [
    {code: '1', name: 'DAIRYWALA_1'},
    {code: '2', name: 'DAIRYWALA_2'}
];

export const helkariNameOptions = [
    {code: '1', name: 'HELKARI_1'},
    {code: '2', name: 'HELKARI_2'}
];

export const vehicleTypeOptions = [
    {code: '1', name: 'VEHICLE_1'},
    {code: '2', name: 'VEHICLE_2'},
    {code: '3', name: 'VEHICLE_3'},
    {code: '4', name: 'VEHICLE_4'},
]

export const vehicleStatusOptions = [
    {code: '1', name: 'IN'},
    {code: '2', name: 'OUT'}
];

export const shadeNumberOptions = [
    {code: '1', name: 'SHADE_1'},
    {code: '2', name: 'SHADE_2'}
];

export const parkingMockData = {
    vehicleType: {code: '1', name: 'VEHICLE_1'},
    vehicleNumber: 'MH-11 1111',
    parkingDate: new Date().toISOString().split('T')[0],
    parkingTime: new Date().toTimeString().split(' ')[0],
    parkingAmount: 100
};

const tenDaysAgo = new Date();
tenDaysAgo.setDate(tenDaysAgo.getDate() - 10);

export const arrivalMockData = {
    importType: { code: '1', name: 'IMPORT_TYPE_1' },
    importPermissionNumber: "123456",
    importPermissionDate: tenDaysAgo.toISOString().split('T')[0],
    traderName: traderNameOptions[0],
    licenseNumber: "LIC123",
    vehicleNumber: 'MH-11 1111',
    numberOfAliveAnimals: 2,
    numberOfDeadAnimals: 1,
    arrivalDate: new Date().toISOString().split('T')[0],
    arrivalTime: new Date().toTimeString().split(' ')[0],
    gawalName: gawalNameOptions[0]
};

export const salsetteRemovalMockData = {
    traderName: { code: '1', name: 'TRADER_1' },
    brokerName: { code: '1', name: 'BROKER_1' },
    gawalName: { code: '1', name: 'GAWAL_1'},
    dairywalaName: { code: '1', name: 'DAIRYWALA_1'},
    numberOfAnimals: 1,
    animalTokenNumber: 1,
    removalDate: new Date().toISOString().split('T')[0],
    removalTime: new Date().toTimeString().split(' ')[0]
};

export const collectionSalsetteMockData = {
    removalType: {code: '1', name: 'SALSETTE'},
    traderName: { code: '1', name: 'TRADER_1' },
    brokerName: { code: '1', name: 'BROKER_1' },
    gawalName: { code: '1', name: 'GAWAL_1'},
    dairywalaName: { code: '1', name: 'DAIRYWALA_1'},
    numberOfAnimals: 1,
    animalTokenNumber: 1,
    removalFeeAmount: 0,
    paymentMode: paymentModeOptions[0],
    referenceNumber: "12345"
};

export const collectionRemovalFeeAmt = {
    collectionRemovalFeeAmt: 100
};

export const stablingFeeAmt = {
    amount: 10
};

export const slaughterFeeAmt = {
    amount: 100
};

export const religiousPersonalMockData = {
    traderName: { code: '1', name: 'TRADER_1' },
    brokerName: { code: '1', name: 'BROKER_1' },
    gawalName: { code: '1', name: 'GAWAL_1'},
    citizenName: 'John Doe',
    numberOfAnimals: 1,
    animalTokenNumber: 1,
    removalDate: new Date().toISOString().split('T')[0],
    removalTime: new Date().toTimeString().split(' ')[0]
};

export const notSoldMockData = {
    traderName: { code: '1', name: 'TRADER_1' },
    brokerName: { code: '1', name: 'BROKER_1' },
    gawalName: { code: '1', name: 'GAWAL_1'},
    numberOfAnimals: 1,
    animalTokenNumber: 1,
    removalDate: new Date().toISOString().split('T')[0],
    removalTime: new Date().toTimeString().split(' ')[0]
};

export const rejectionBeforeTradingMockData = {
    traderName: { code: '1', name: 'TRADER_1' },
    brokerName: { code: '1', name: 'BROKER_1' },
    gawalName: { code: '1', name: 'GAWAL_1'},
    numberOfAnimals: 1,
    animalTokenNumber: 1,
    removalDate: new Date().toISOString().split('T')[0],
    removalTime: new Date().toTimeString().split(' ')[0]
};

export const rejectionAfterTradingMockData = {
    shopkeeperName: shopkeeperNameOptions[0],
    dawanwalaName: dawanwalaNameOptions[0],
    numberOfAnimals: 1,
    animalTokenNumber: 1,
    removalDate: new Date().toISOString().split('T')[0],
    removalTime: new Date().toTimeString().split(' ')[0]
};

export const deathBeforeTradingMockData = rejectionBeforeTradingMockData;

export const deathAfterTradingMockData = rejectionAfterTradingMockData;

export const paymentModeOptions = [
    {code: '1', name: 'PAYMENT_MODE_1'},
    {code: '2', name: 'PAYMENT_MODE_2'}
];

export const slaughterInAbbatoirMockData = {
    traderName: traderNameOptions[0],
    brokerName: brokerNameOptions[0],
    gawalName: gawalNameOptions[0],
    assignDate: new Date().toISOString().split('T')[0],
    shopkeeperName: shopkeeperNameOptions[0],
    dawanwalaName: dawanwalaNameOptions[0],
    shadeNumber: 1,
    numberOfAliveAnimals: 1,
    animalTokenNumber: 1,
    stablingDays: 2,
    stablingFeeAmount: 100,
    paymentMode: paymentModeOptions[0],
    paymentReferenceNumber: 1
};

export const religiousPersonalRemovalMockData = {
    traderName: traderNameOptions[0],
    brokerName: brokerNameOptions[0],
    gawalName: gawalNameOptions[0],
    assignDate: new Date().toISOString().split('T')[0],
    citizenName: 'John Doe',
    numberOfAnimals: 1,
    animalTokenNumber: 1,
    removalFeeAmount: 100,
    paymentMode: paymentModeOptions[0],
    paymentReferenceNumber: 100
};

export const collectionReligiousPersonalRemovalMockData = {
    traderName: traderNameOptions[0],
    brokerName: brokerNameOptions[0],
    gawalName: gawalNameOptions[0],
    citizenName: 'John Doe',
    numberOfAnimals: 1,
    animalTokenNumber: 1,
    removalFeeAmount: 100,
    paymentMode: paymentModeOptions[0],
    paymentReferenceNumber: 100
};

export const salsetteRemovalShopkeeperAssignmentMockData = {
    traderName: traderNameOptions[0],
    brokerName: brokerNameOptions[0],
    gawalName: gawalNameOptions[0],
    assignDate: new Date().toISOString().split('T')[0],
    dairywalaName: dairywalaNameOptions[0],
    numberOfAnimals: 1,
    animalTokenNumber: 1,
    salsetteFeeAmount: 100,
    paymentMode: paymentModeOptions[0],
    paymentReferenceNumber: 1
};

export const entryFeeCollectionMockData = {
    arrivalUuid: 'abc1234567890',
    traderName: traderNameOptions[0],
    licenseNumber: '123456abc',
    vehicleNumber: 'MH 11 1234',
    numberOfAliveAnimals: 1,
    arrivalDate: new Date().toISOString().split('T')[0],
    arrivalTime: new Date().toTimeString().split(' ')[0],
    entryFee: '100',
};

export const collectionRemovalOfNotSoldAnimalsMockData = {
    traderName: traderNameOptions[0],
    brokerName: brokerNameOptions[0],
    gawalName: gawalNameOptions[0],
    numberOfAliveAnimals: 1,
    animalTokenNumber: 1,
    removalFeeAmount: 100,
    paymentMode: paymentModeOptions[0],
    paymentReferenceNumber: 1
};

export const collectionRemovalOfRejectedBeforeMockData = {
    traderName: traderNameOptions[0],
    brokerName: brokerNameOptions[0],
    gawalName: gawalNameOptions[0],
    removalFeeAmount: 100,
    paymentMode: paymentModeOptions[0],
    paymentReferenceNumber: 1
};

export const collectionRemovalOfRejectedAfterMockData = {
    shopkeeperName: shopkeeperNameOptions[0],
    dawanwalaName: dawanwalaNameOptions[0],
    numberOfAnimals: 1,
    animalTokenNumber: 1,
    removalFeeAmount: 100,
    paymentMode: paymentModeOptions[0],
    paymentReferenceNumber: 1
};

export const collectionRemovalDeathBeforeMockdata = collectionRemovalOfRejectedBeforeMockData;

export const removalDeathAfterMockdata = collectionRemovalOfRejectedAfterMockData;

export const stablingBeforeTradingMockdata = {
    brokerName: brokerNameOptions[0],
    shadeNumber: shadeNumberOptions[0],
};

export const stablingAfterMockdata = {
    dawanwalaName: dawanwalaNameOptions[0],
    shopkeeperName: shopkeeperNameOptions[0],
};

export const daysOfWeek = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];

export const species = [
    {code: '1', name: 'SPECIES_1'},
    {code: '2', name: 'SPECIES_2'},
    {code: '3', name: 'NAN'}
];

export const breed = [
    {code: '1', name: 'BREED_1'},
    {code: '2', name: 'BREED_2'},
    {code: '3', name: 'NAN'}
];

export const gait = [
    {code: '1', name: 'GAIT_1'},
    {code: '2', name: 'GAIT_2'},
    {code: '3', name: 'NAN'}
];

export const posture = [
    {code: '1', name: 'POSTURE_1'},
    {code: '2', name: 'POSTURE_2'},
    {code: '3', name: 'NAN'}
];

export const sex = [
    {code: '1', name: 'FEMALE'},
    {code: '2', name: 'MALE'},
    {code: '3', name: 'NAN'}
];

export const bodyColor = [
    {code: '1', name: 'WHITE'},
    {code: '2', name: 'BLACK'},
    {code: '3', name: 'BROWN'},
    {code: '3', name: 'NAN'}
];

export const pregnancy = [
    { code: true, name: "PREGNANT" },
    { code: false, name: "NOT_PREGNANT" },
];

export const bodyTemperature = [
    {code: '1', name: 'TEMPERATURE_1'},
    {code: '2', name: 'TEMPERATURE_2'},
    {code: '3', name: 'NAN'}
];

export const pulse = [
    {code: '1', name: 'PULSE_1'},
    {code: '2', name: 'PULSE_2'},
    {code: '3', name: 'NAN'}
];

export const appetite = [
    {code: '1', name: 'APPETITE_1'},
    {code: '2', name: 'APPETITE_2'},
    {code: '3', name: 'NAN'}
];

export const eyes = [
    {code: '1', name: 'EYES_1'},
    {code: '2', name: 'EYES_2'},
    {code: '3', name: 'NAN'}
];

export const nostrils = [
    {code: '1', name: 'NOSTRILS_1'},
    {code: '2', name: 'NOSTRILS_2'},
    {code: '3', name: 'NAN'}
];

export const muzzle = [
    {code: '1', name: 'MUZZLE_1'},
    {code: '2', name: 'MUZZLE_2'},
    {code: '3', name: 'NAN'}
];

export const opinion = [
    {code: '1', name: 'OPINION_1'},
    {code: '2', name: 'OPINION_2'},
    {code: '3', name: 'NAN'}
];

export const animalStabling = [
    {code: '1', name: 'STABLING_1'},
    {code: '2', name: 'STABLING_2'},
    {code: '3', name: 'NAN'}
];

export const visibleMucusMembrane = [
    {code: '1', name: 'MUCUS_PRESENT'},
    {code: '2', name: 'MUCUS_ABSENT'},
    {code: '3', name: 'NAN'}
];

export const thoracicCavity = [
    {code: '1', name: 'OPTION_1'},
    {code: '2', name: 'OPTION_2'},
    {code: '3', name: 'NAN'}
];

export const abdominalCavity = [
    {code: '1', name: 'OPTION_1'},
    {code: '2', name: 'OPTION_2'},
    {code: '3', name: 'NAN'}
];

export const pelvicCavity = [
    {code: '1', name: 'OPTION_1'},
    {code: '2', name: 'OPTION_2'},
    {code: '3', name: 'NAN'}
];

export const specimenCollection = [
    {code: '1', name: 'YES'},
    {code: '2', name: 'NO'},
    {code: '3', name: 'NAN'}
];

export const specialObservation = [
    {code: '1', name: 'YES'},
    {code: '2', name: 'NO'},
    {code: '3', name: 'NAN'}
];

export const anteMortemInspectionMockData = {
    arrivalUuid: 'abc1234567890',
    slaughterReceiptNumber: '1234567',
    anteMortemInspectionDate: new Date().toISOString().split('T')[0],
    anteMortemInspectionDay: daysOfWeek[new Date().getDay()],
    traderName: traderNameOptions[0],
    licenseNumber: "123456",
    numberOfAliveAnimals: 1,
    animalTokenNumber: 455,
    species: species[2],
    breed: breed[2],
    sex: sex[2],
    bodyColor: bodyColor[3],
    pregnancy: pregnancy[1],
    approximateAge: 0,
    gait: gait[2],
    posture: posture[2],
    bodyTemperature: bodyTemperature[2],
    pulseRate: pulse[2],
    appetite: appetite[2],
    eyes: eyes[2],
    nostrils: nostrils[2],
    muzzle: muzzle[2],
    opinion: opinion[2],
    animalStabling: animalStabling[2],
    other: "ok",
    remark: "ok"
};

export const anteMortemPreInspectionMockData = {
    anteMortemInspectionDate: new Date().toISOString().split('T')[0],
    anteMortemInspectionDay: daysOfWeek[new Date().getDay()],
    slaughterReceiptNumber: '1234567',
    numberOfAliveAnimals: 1,
    animalTokenNumber: 455,
    species: species[2],
    breed: breed[2],
    sex: sex[2],
    bodyColor: bodyColor[3],
    pregnancy: pregnancy[1],
    approximateAge: 0,
    gait: gait[2],
    posture: posture[2],
    bodyTemperature: bodyTemperature[2],
    pulseRate: pulse[2],
    appetite: appetite[2],
    eyes: eyes[2],
    nostrils: nostrils[2],
    muzzle: muzzle[2],
    opinion: opinion[2],
    animalStabling: animalStabling[2],
    other: "ok",
    remark: "ok"
};

export const postMortemMockData = {
    postMortemInspectionDate: new Date().toISOString().split('T')[0],
    postMortemInspectionDay: daysOfWeek[new Date().getDay()],
    slaughterReceiptNumber: '1234567',
    numberOfAnimals: 0,
    animalTokenNumber: 455,
    species: species[2],
    breed: breed[2],
    sex: sex[2],
    bodyColor: bodyColor[3],
    pregnancy: pregnancy[1],
    approximateAge: 0,
    visibleMucusMembrane: visibleMucusMembrane[2],
    thoracicCavity: thoracicCavity[2],
    abdominalCavity: abdominalCavity[2],
    pelvicCavity: pelvicCavity[2],
    specimenCollection: specimenCollection[2],
    specialObservation: specialObservation[2],
    opinion: opinion[2],
    other: "ok",
    remark: "ok"
};

export const slaughterType = [
    {code: '1', name: 'SLAUGHTER_TYPE_1'},
    {code: '2', name: 'SLAUGHTER_TYPE_2'}
];

export const slaughterUnit = [
    {code: '1', name: 'SLAUGHTER_UNIT_1'},
    {code: '2', name: 'SLAUGHTER_UNIT_2'}
];

export const slaughterSession = [
    {code: '1', name: 'SLAUGHTER_SESSION_1'},
    {code: '2', name: 'SLAUGHTER_SESSION_2'}
];

export const slaughterFeeRecoveryMockData = {
    slaughterType: slaughterType[0],
    slaughterUnit: slaughterUnit[0],
    slaughterSession: slaughterSession[0],
    shopkeeperName: shopkeeperNameOptions[0],
    dawanwalaName: dawanwalaNameOptions[0],
    helkariName: helkariNameOptions[0],
    numberOfAnimals: 0,
    animalTokenNumber: 123,
    slaughterFeeAmount: 0,
    paymentMode: paymentModeOptions[0],
    referenceNumber: 123456
};

export const typeOfAnimal = [
    {code: '1', name: 'ANIMAL_1'},
    {code: '2', name: 'ANIMAL_2'}
];

export const weighingChargeMockData = {
    typeOfAnimal: typeOfAnimal[0],
    carcassWeight: 0,
    kenaWeight: 0,
    paymentMode: paymentModeOptions[0],
    referenceNumber: 123
};

export const vehicleWashingAmount = {
    amount: 100
};

export const vehicleWashingMockData = {
    vehicleType: vehicleTypeOptions[0],
    vehicleNumber: 'MH-11 1111',
    washingChargeAmount: 100,
    paymentMode: paymentModeOptions[0],
    referenceNumber: 123
};

export const penaltyTypes = [
    {code: '1', name: 'PENALTY_TYPE_1'},
    {code: '2', name: 'PENALTY_TYPE_2'}
];

export const penaltyChargeAmt = {
    amount: 100
};

export const penaltyChargeMockdata = {
    typeOfPenalty: penaltyTypes[0],
    penaltyChargeAmount: penaltyChargeAmt.amount,
    paymentMode: paymentModeOptions[0],
    referenceNumber: 123
};

export const meatType = [
    {code: '1', name: "CARCASS"},
    {code: '2', name: "KENA"}
];

export const gatePassMockData = {
    vehicleType: vehicleTypeOptions[0],
    vehicleNumber: 'MH11AA1111',
    receiverName: "John Doe",
    receiverContact: "9898989897",
    numberOfAnimals: 2,
    typeOfMeat: meatType[0],
    typeOfAnimal: typeOfAnimal[0],
    weight: 5,
    referenceNumber: 123,
    shopkeeperName: shopkeeperNameOptions[0],
    helkariName: helkariNameOptions[0],
};

