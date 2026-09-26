import React from 'react';
import { FiCheck } from 'react-icons/fi';

const StatusStepper = ({ steps, currentStatus }) => {
  const currentIndex = steps.findIndex(step => step.status === currentStatus);
  
  return (
    <div className="w-full py-6">
      <div className="flex items-center">
        {steps.map((step, index) => {
          const isCompleted = index < currentIndex || currentStatus === 'COMPLETED';
          const isCurrent = index === currentIndex;
          
          return (
            <React.Fragment key={step.status}>
              <div className="relative flex flex-col items-center group">
                <div className={`
                  flex items-center justify-center w-10 h-10 rounded-full border-2 transition-all duration-300
                  ${isCompleted ? 'bg-primary-600 border-primary-600 text-white' : 
                    isCurrent ? 'bg-white border-primary-600 text-primary-600 shadow-md scale-110' : 
                    'bg-white border-gray-300 text-gray-400'}
                `}>
                  {isCompleted ? <FiCheck className="w-6 h-6" /> : <span>{index + 1}</span>}
                </div>
                <div className={`
                  absolute top-12 whitespace-nowrap text-xs font-semibold uppercase tracking-wider transition-colors
                  ${isCurrent ? 'text-primary-600' : 'text-gray-500'}
                `}>
                  {step.label}
                </div>
              </div>
              
              {index < steps.length - 1 && (
                <div className={`flex-1 h-0.5 mx-2 transition-colors duration-500 ${index < currentIndex ? 'bg-primary-600' : 'bg-gray-200'}`}></div>
              )}
            </React.Fragment>
          );
        })}
      </div>
    </div>
  );
};

export default StatusStepper;
