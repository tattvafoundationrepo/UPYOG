// src/redux/reducers/securityReducer.js

import { FETCH_DEONAR_DETAILS_SUCCESS, FETCH_DEONAR_ENTRY_FEE_DETAILS_SUCCESS, FETCH_STAKEHOLDER_DETAILS_SUCCESS, SAVE_DEONAR_DETAILS_FAILURE, SAVE_DEONAR_DETAILS_SUCCESS } from "../actions/types";


  
  const initialState = {
    details: {},
    stakeholderDetails: {},
    saveSuccess: false,
    saveError: null,
    entryFeeDetails: {}
  };
  
  const securityReducer = (state = initialState, action) => {
    switch (action.type) {
      case FETCH_DEONAR_DETAILS_SUCCESS:
        return { ...state, details: action.payload };
  
      case FETCH_STAKEHOLDER_DETAILS_SUCCESS:
        return { ...state, stakeholderDetails: action.payload };
  
      case SAVE_DEONAR_DETAILS_SUCCESS:
        return { ...state, saveSuccess: true, saveError: null };
  
      case SAVE_DEONAR_DETAILS_FAILURE:
        return { ...state, saveSuccess: false, saveError: action.payload };

      case FETCH_DEONAR_ENTRY_FEE_DETAILS_SUCCESS:
        console.log('Action payload:', action.payload); // Log payload
        return { ...state, entryFeeDetails: action.payload };
      default:
        return state;
    }
  };
  
  export default securityReducer;
  