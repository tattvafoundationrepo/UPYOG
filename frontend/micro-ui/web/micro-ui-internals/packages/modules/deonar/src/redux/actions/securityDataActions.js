import { FETCH_DEONAR_DETAILS_SUCCESS, FETCH_DEONAR_ENTRY_FEE_DETAILS_SUCCESS, FETCH_STAKEHOLDER_DETAILS_SUCCESS, SAVE_DEONAR_DETAILS_FAILURE, SAVE_DEONAR_DETAILS_SUCCESS } from "./types";

// src/redux/actions/securityActions.js
export const fetchDeonarDetailsSuccess = (data) => ({
  type: FETCH_DEONAR_DETAILS_SUCCESS,
  payload: data,
});

export const fetchStakeholderDetailsSuccess = (data) => ({
  type: FETCH_STAKEHOLDER_DETAILS_SUCCESS,
  payload: data,
});

export const saveDeonarDetailsSuccess = () => ({
  type: SAVE_DEONAR_DETAILS_SUCCESS,
});

export const saveDeonarDetailsFailure = (error) => ({
  type: SAVE_DEONAR_DETAILS_FAILURE,
  payload: error,
});

export const fetchDeonarEntryFeeDetailsSuccess = (data) => {
  return {
    type: FETCH_DEONAR_ENTRY_FEE_DETAILS_SUCCESS,
    payload: data,
  };
}

