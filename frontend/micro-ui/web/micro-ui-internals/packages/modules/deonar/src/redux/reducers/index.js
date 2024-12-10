import { combineReducers } from "redux";
import securityReducer from "./securityDataReducer";

const getRootReducer = () =>
  combineReducers({
    security: securityReducer,
    deonarSecurity: {}
  });

export default getRootReducer;
