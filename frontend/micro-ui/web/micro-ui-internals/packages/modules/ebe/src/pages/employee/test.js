import React, { useState } from "react";
import MultiColumnDropdown from "../../components/MultiColumnDropdown"; // Import your updated Dropdown component

const ExampleDropdownUsage = () => {
 // Initialize state for selected options
 const [selectedOption, setSelectedOption] = useState([]); // Empty array for multi-select, or empty object for single select

 const dropdownOptions = [
   { label: "Option 1", value: "test1", type1: 1, type2: 2 , type3: 1},
   { label: "Option 2", value: "test2", type1: 1, type2: 2 , type3: 2},
   { label: "Option 3", value: "test3", type1: 1, type2: 2 , type3: 3},
 ];

 // Function to handle selection from dropdown
 const handleSelect = (e, selectedOptions) => {
   console.log("Selected options:", selectedOptions); // This should log the selected options
   setSelectedOption(selectedOptions);  // Update the selectedOption state
 };

 return (
   <div>
     <h3>Select an Option</h3>
     <MultiColumnDropdown
       options={dropdownOptions}
       selected={selectedOption} // Pass the selected options
       onSelect={handleSelect}   // Function to handle selection
       displayKeys={['label', 'type1', 'type3']} // Specify which keys to display
       defaultLabel="Select an option"
       defaultUnit="Options"
       optionsKey="value"
       autoCloseOnSelect={true}
       showColumnHeaders={true}
       headerMappings={{ type1: "First Type", type3: "Third Type" }}
     />
   </div>
 );
};


export default ExampleDropdownUsage;